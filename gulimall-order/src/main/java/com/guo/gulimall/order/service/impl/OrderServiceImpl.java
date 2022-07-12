package com.guo.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.constant.OrderConstant;
import com.guo.common.excepiton.NoStockException;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.common.utils.R;
import com.guo.common.vo.MemberRespVO;
import com.guo.gulimall.order.dao.OrderDao;
import com.guo.gulimall.order.entity.OrderEntity;
import com.guo.gulimall.order.entity.OrderItemEntity;
import com.guo.gulimall.order.feign.CartFeignService;
import com.guo.gulimall.order.feign.MemberFeignService;
import com.guo.gulimall.order.feign.ProductFeignService;
import com.guo.gulimall.order.feign.WareFeignService;
import com.guo.gulimall.order.interceptor.LoginUserInterceptor;
import com.guo.gulimall.order.service.OrderItemService;
import com.guo.gulimall.order.service.OrderService;
import com.guo.gulimall.order.to.OrderCreateTO;
import com.guo.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {


    private ThreadLocal<OrderSubmitVO> orderSubmitVOThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public OrderConfirmVO confirmOrder() {
        OrderConfirmVO confirmVO = new OrderConfirmVO();
        MemberRespVO memberRespVO = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

        // 远程查询所有收获地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVO> address = memberFeignService.getAddress(memberRespVO.getId());
            confirmVO.setAddressList(address);
        }, executor);

        // 远程查询所有购物项
        CompletableFuture<Void> cartItemsFuture = CompletableFuture
                .runAsync(() -> {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    List<OrderItemVO> cartItems = cartFeignService.getCurrentUserCartItems();
                    confirmVO.setItemList(cartItems);
                }, executor)
                .thenRunAsync(() -> {
                    List<OrderItemVO> itemList = confirmVO.getItemList();
                    List<Long> skuIds = itemList.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
                    R r = wareFeignService.getSkuHasStock(skuIds);
                    List<SkuStockVO> skuStockVOS = r.getData(new TypeReference<List<SkuStockVO>>() {
                    });
                    if (!CollectionUtils.isEmpty(skuStockVOS)) {
                        Map<Long, Boolean> skuStockMap = skuStockVOS.stream().collect(Collectors.toMap(SkuStockVO::getSkuId, SkuStockVO::getHasStock));
                        confirmVO.setSkuStockMap(skuStockMap);
                    }
                }, executor);
        try {
            CompletableFuture.allOf(addressFuture, cartItemsFuture).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        confirmVO.setIntegration(memberRespVO.getIntegration());

        // 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVO.getId(), token, 3, TimeUnit.MINUTES);
        confirmVO.setOrderToken(token);

        return confirmVO;
    }


    @GlobalTransactional
    @Transactional()
    @Override
    public SubmitOrderResponseVO submitOrder(OrderSubmitVO orderSubmitVO) {

        orderSubmitVOThreadLocal.set(orderSubmitVO);
        SubmitOrderResponseVO responseVO = new SubmitOrderResponseVO();
        MemberRespVO memberRespVO = LoginUserInterceptor.loginUser.get();
        // 下单：创建订单、验证令牌、验价、锁库存
        String orderToken = orderSubmitVO.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end;";
        String key = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVO.getId();
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(key), orderToken);
        if (result != null && result == 0) {
            // 验证令牌失败
            responseVO.setCode(1);
        } else {
            // 创建订单
            OrderCreateTO order = createOrder();
            // 保存订单
            saveOrder(order);
            // 库存锁定
            WareSkuLockVO wareSkuLockVO = new WareSkuLockVO();
            wareSkuLockVO.setOrderSn(order.getOrder().getOrderSn());
            List<OrderItemVO> orderItemVOList = order.getOrderItems()
                    .stream()
                    .map(e -> {
                        OrderItemVO vo = new OrderItemVO();
                        vo.setSkuId(e.getSkuId());
                        vo.setCount(e.getSkuQuantity());
                        vo.setTitle(e.getSkuName());
                        return vo;
                    })
                    .collect(Collectors.toList());
            wareSkuLockVO.setLocks(orderItemVOList);
            R r = wareFeignService.orderLockStock(wareSkuLockVO);
            if (r.getCode() == 0) {
                // 锁定成功
                // 模拟远程扣除积分异常
                int i = 1 / 0;
                responseVO.setOrder(order.getOrder());
                responseVO.setCode(0);
            } else {
                // 失败
                throw new NoStockException("库存不足");
            }
        }
        return responseVO;
    }

    private void saveOrder(OrderCreateTO order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTO createOrder() {

        OrderCreateTO orderCreateTO = new OrderCreateTO();
        // 订单号
        String orderSn = IdWorker.getTimeId();
        // 构建订单
        OrderEntity orderEntity = buildOrder(orderSn);
        // 构建订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        // 算价
        computePrice(orderEntity, orderItemEntities);

        orderCreateTO.setOrder(orderEntity);
        orderCreateTO.setOrderItems(orderItemEntities);

        return orderCreateTO;

    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //总价
        BigDecimal total = BigDecimal.ZERO;
        //优惠价格
        BigDecimal promotion=new BigDecimal("0.0");
        BigDecimal integration=new BigDecimal("0.0");
        BigDecimal coupon=new BigDecimal("0.0");
        //积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total = total.add(orderItemEntity.getRealAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            integration = integration.add(orderItemEntity.getIntegrationAmount());
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            integrationTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //付款价格=商品价格+运费
        orderEntity.setPayAmount(orderEntity.getFreightAmount().add(total));

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(LoginUserInterceptor.loginUser.get().getId());
        OrderSubmitVO orderSubmitVO = orderSubmitVOThreadLocal.get();
        R r = wareFeignService.getFare(orderSubmitVO.getAddressId());
        FareVO fareVO = r.getData(new TypeReference<FareVO>() {
        });
        orderEntity.setFreightAmount(fareVO.getFare());
        orderEntity.setReceiverCity(fareVO.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareVO.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareVO.getAddress().getName());
        orderEntity.setReceiverPhone(fareVO.getAddress().getPhone());
        orderEntity.setReceiverProvince(fareVO.getAddress().getProvince());
        orderEntity.setReceiverPostCode(fareVO.getAddress().getPostCode());
        orderEntity.setReceiverRegion(fareVO.getAddress().getRegion());
        return orderEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVO> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (!CollectionUtils.isEmpty(currentUserCartItems)) {
           return currentUserCartItems
                    .stream()
                    .map(e -> {
                        OrderItemEntity orderItemEntity = buildOrderItem(e);
                        orderItemEntity.setOrderSn(orderSn);
                        return orderItemEntity;
                    }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private OrderItemEntity buildOrderItem(OrderItemVO orderItemVO) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 商品的 spu 信息
        R r = productFeignService.getSpuInfoBySkuId(orderItemVO.getSkuId());
        SpuInfoVO spuInfoVO = r.getData(new TypeReference<SpuInfoVO>() {
        });
        if (spuInfoVO != null) {
            orderItemEntity.setSpuId(spuInfoVO.getId());
            orderItemEntity.setSpuName(spuInfoVO.getSpuName());
            orderItemEntity.setSpuBrand(spuInfoVO.getBrandId().toString());
            orderItemEntity.setCategoryId(spuInfoVO.getCatalogId());
        }
        // 商品的 sku 信息
        orderItemEntity.setSkuId(orderItemVO.getSkuId());
        orderItemEntity.setSkuName(orderItemVO.getTitle());
        orderItemEntity.setSkuPic(orderItemVO.getImage());
        orderItemEntity.setSkuPrice(orderItemVO.getPrice());
        orderItemEntity.setSkuQuantity(orderItemVO.getCount());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(orderItemVO.getSkuAttr(), ";"));
        // 优惠信息（不做）

        // 订单项订单价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);
        // 积分信息
        orderItemEntity.setGiftGrowth(orderItemVO.getPrice().intValue());
        orderItemEntity.setGiftIntegration(orderItemVO.getPrice().intValue());
        BigDecimal originPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realPrice = originPrice.subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realPrice);
        return orderItemEntity;
    }

}