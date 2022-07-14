package com.guo.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.excepiton.NoStockException;
import com.guo.common.to.SkuHasStockTo;
import com.guo.common.to.mq.OrderTO;
import com.guo.common.to.mq.StockLockedTO;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.common.utils.R;
import com.guo.gulimall.ware.dao.WareSkuDao;
import com.guo.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.guo.gulimall.ware.entity.WareOrderTaskEntity;
import com.guo.gulimall.ware.entity.WareSkuEntity;
import com.guo.gulimall.ware.enums.WareTaskStatusEnum;
import com.guo.gulimall.ware.feign.OrderFeignService;
import com.guo.gulimall.ware.feign.ProductFeignService;
import com.guo.gulimall.ware.service.WareOrderTaskDetailService;
import com.guo.gulimall.ware.service.WareOrderTaskService;
import com.guo.gulimall.ware.service.WareSkuService;
import com.guo.gulimall.ware.vo.OrderItemVO;
import com.guo.gulimall.ware.vo.WareSkuLockVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RabbitListener(queues = {"stock.release.queue"})
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    WareOrderTaskService orderTaskService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Override
    public void unLockStock(StockLockedTO stockLockedTO) {

        // 如果工作单记录在 DB 中查不到，说明库存锁定成功但是发生异常回滚了，这种情况无需解锁
        WareOrderTaskDetailEntity orderDetail = orderTaskDetailService.getById(stockLockedTO.getTaskDetailId());
        if (orderDetail != null) {
            // 如果存在，说明库存锁定成功，则根据订单情况分类
            // 如果订单不存在，必须解锁；如果订单存在：状态为已取消，解锁，否则不解锁
            WareOrderTaskEntity orderTask = orderTaskService.getById(stockLockedTO.getTaskId());
            // 查询订单信息
            Integer status = orderFeignService.getOrderStatus(orderTask.getOrderSn());
            // 订单被取消
            if (status == null || status == 4) {
                unLockStock(orderDetail.getSkuId(), orderDetail.getWareId(), orderDetail.getSkuNum(), orderDetail.getId());
            }
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long orderDetailId) {
        baseMapper.unLockStock(skuId, wareId, skuNum);
        log.info("解锁 sku={} wareId={} 库存{}件", skuId, wareId, skuNum);
        WareOrderTaskDetailEntity detail = WareOrderTaskDetailEntity
                .builder()
                .id(orderDetailId)
                .lockStatus(WareTaskStatusEnum.hasUnLocked.getCode())// 变为已解锁
                .build();
        orderTaskDetailService.updateById(detail);
    }

    @Override
    public void unLockStock(OrderTO orderTO) {

        // 查询库存工作单
        WareOrderTaskEntity orderTask = orderTaskService.lambdaQuery().eq(WareOrderTaskEntity::getOrderSn, orderTO.getOrderSn()).one();
        List<WareOrderTaskDetailEntity> unLockOrderList = orderTaskDetailService.lambdaQuery()
                .eq(WareOrderTaskDetailEntity::getTaskId, orderTask.getId())
                .eq(WareOrderTaskDetailEntity::getLockStatus, WareTaskStatusEnum.Locked.getCode())
                .list();
        unLockOrderList.forEach(e -> unLockStock(e.getSkuId(), e.getWareId(), e.getSkuNum(), e.getId()));
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }

            save(skuEntity);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(id -> {
            SkuHasStockTo stockDto = new SkuHasStockTo();
            LambdaQueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<WareSkuEntity>()
                    .lambda()
                    .select(WareSkuEntity::getStockLocked)
                    .eq(WareSkuEntity::getSkuId, id);
            int count = count(queryWrapper);
            stockDto.setSkuId(id);
            stockDto.setHasStock(count > 0);
            return stockDto;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = NoStockException.class) // 默认运行时异常都会回滚
    @Override
    public boolean orderLockStock(WareSkuLockVO wareSkuLockVO) {

        // 保存库存工作单的详情（追溯用）
        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity();
        orderTaskEntity.setOrderSn(wareSkuLockVO.getOrderSn());
        orderTaskService.save(orderTaskEntity);

        List<OrderItemVO> locks = wareSkuLockVO.getLocks();
        List<SkuWareHasStock> skuWareHasStocks = locks.stream()
                .map(e -> {
                    SkuWareHasStock stock = new SkuWareHasStock();
                    Long skuId = e.getSkuId();
                    stock.skuId = skuId;
                    List<Long> wareIds = baseMapper.listWareWithSkuStock(skuId);
                    stock.wareIds = wareIds;
                    stock.num = e.getCount();
                    return stock;
                })
                .collect(Collectors.toList());
        // 锁定库存
        boolean allStocked = true;
        for (SkuWareHasStock hasStock : skuWareHasStocks) {
            boolean skuStocked = false;
            Long skuId = hasStock.skuId;
            if (hasStock.wareIds == null || hasStock.wareIds.isEmpty()) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : hasStock.wareIds) {
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.num);
                // 当前仓库锁定失败，重试下一个仓库
                if (count == 1) {
                    skuStocked = true;
                    // 通知 MQ 库存锁定成功
                    // 如果锁定成功，将当前商品的锁定的工作单记录发给MQ
                    // 如果锁定失败，前面保存的工作单信息就回滚了。发送 MQ 的信息，即使想要解锁，在 DB 中也查询不到记录，无法解锁
                    WareOrderTaskDetailEntity orderTaskDetailEntity = WareOrderTaskDetailEntity.builder()
                            .skuId(skuId)
                            .wareId(wareId)
                            .skuName("")
                            .skuNum(hasStock.num)
                            .taskId(orderTaskEntity.getId())
                            .lockStatus(1)
                            .build();
                    orderTaskDetailService.save(orderTaskDetailEntity);
                    StockLockedTO stockLockedTO = new StockLockedTO();
                    stockLockedTO.setTaskId(orderTaskEntity.getId());
                    stockLockedTO.setTaskDetailId(orderTaskDetailEntity.getId());
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTO);
                    break;
                }
            }

            if (!skuStocked) {
                // 当前商品所有仓库都没锁住
                throw new NoStockException(skuId);

            }
        }
        return true;
    }

    class SkuWareHasStock {
        Long skuId;
        List<Long> wareIds;

        Integer num;
    }
}