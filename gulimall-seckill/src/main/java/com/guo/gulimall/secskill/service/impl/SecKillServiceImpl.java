package com.guo.gulimall.secskill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.guo.common.to.SecKillOrderTO;
import com.guo.common.utils.R;
import com.guo.common.vo.MemberRespVO;
import com.guo.gulimall.secskill.feign.CouponFeignService;
import com.guo.gulimall.secskill.feign.ProductFeignService;
import com.guo.gulimall.secskill.interceptor.LoginInterceptor;
import com.guo.gulimall.secskill.service.SecKillService;
import com.guo.gulimall.secskill.to.SecKillSkuRedisTO;
import com.guo.gulimall.secskill.vo.SecKillSessionWithSkusVO;
import com.guo.gulimall.secskill.vo.SecKillSkuVO;
import com.guo.gulimall.secskill.vo.SkuInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SecKillServiceImpl implements SecKillService {

    private final CouponFeignService couponFeignService;

    private final ProductFeignService productFeignService;

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redissonClient;

    private final RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "secKill:sessions:";

    private final String SKU_CACHE_PREFIX = "secKill:skus";

    // sku 库存信号量
    private final String SKU_STOCK_SEMAPHORE = "secKill:stock:";

    @Override
    public void uploadSecKillSkuLatest3Days() {
        // 扫描最近三天需要参与秒杀的活动
        R r = couponFeignService.getSecKillSessionsIn3Days();
        if (r.getCode() == 0) {
            List<SecKillSessionWithSkusVO> sessions = r.getData(new TypeReference<List<SecKillSessionWithSkusVO>>() {
            });
            // 缓存秒杀活动信息
            saveSessionInfo(sessions);
            // 缓存活动的sku信息
            saveSessionSkuInfo(sessions);
        }
    }

    public List<SecKillSkuRedisTO> getCurrentSecKillBlockHandler() {
        log.error("getCurrentSecKill 被限流");
        return null;
    }

    // blockHandler 函数会在原方法被限流/降级/系统保护的时候调用，而 fallback 函数会针对所有类型的异常
    @SentinelResource(value = "getCurrentSecKill", blockHandler = "getCurrentSecKillBlockHandler")
    @Override
    public List<SecKillSkuRedisTO> getCurrentSecKill() {

        long now = System.currentTimeMillis();

        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("-");
            long start = Long.parseLong(s[0]);
            long end = Long.parseLong(s[1]);
            if (now > start && now < end) {
                // 查询当前秒杀场次所有的商品信息
                List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);
                List<String> list = ops.multiGet(range);
                if (list != null) {
                    return list.stream()
                            .map(e -> {
                                return JSON.parseObject((String) e, SecKillSkuRedisTO.class);
                            })
                            .collect(Collectors.toList());
                }
                break;
            }

        }

        return null;
    }

    @Override
    public SecKillSkuRedisTO getSkuSecKillInfo(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);

        Set<String> keys = hashOps.keys();
        if (!CollectionUtils.isEmpty(keys)) {
            String regx = "\\d-" + skuId;
            Optional<String> first = keys.stream().filter(e -> Pattern.matches(regx, e)).findFirst();
            if (first.isPresent()) {
                String val = hashOps.get(first.get());
                SecKillSkuRedisTO skuRedisTO = JSON.parseObject(val, SecKillSkuRedisTO.class);
                if (skuRedisTO != null) {
                    long now = System.currentTimeMillis();
                    if (now < skuRedisTO.getStartTime() || now > skuRedisTO.getEndTime()) {
                        skuRedisTO.setRandomCode(null);
                    }
                    return skuRedisTO;
                }
            }

        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {

        MemberRespVO user = LoginInterceptor.loginUser.get();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);
        String val = hashOps.get(killId);
        if (StringUtils.isEmpty(val)) {
            return null;
        }
        SecKillSkuRedisTO skuRedisTO = JSON.parseObject(val, SecKillSkuRedisTO.class);
        Long startTime = skuRedisTO.getStartTime();
        Long endTime = skuRedisTO.getEndTime();
        long now = System.currentTimeMillis();
        if (now < startTime || now > endTime) {
            return null;
        }
        Long skuId = skuRedisTO.getSkuId();
        Long sessionId = skuRedisTO.getPromotionSessionId();
        String randomCode = skuRedisTO.getRandomCode();
        // 验证合法性
        if (randomCode.equals(key) && killId.equals(sessionId + "-" + skuId)) {
            // 验证购物数量是否合理
            if (num <= skuRedisTO.getSecKillLimit()) {
                // 验证当前用户是否已经买过
                String isBuyKey = user.getId() + "_" + killId;
                long ttl = endTime - now;
                Boolean canBuy = redisTemplate.opsForValue().setIfAbsent(isBuyKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                if (Boolean.TRUE.equals(canBuy)) {
                    // 获取信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                    try {
                        semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                        // 秒杀成功，快速下单，给 MQ 发消息
                        String orderSn = IdWorker.getTimeId();
                        SecKillOrderTO orderTO = new SecKillOrderTO();
                        orderTO.setOrderSn(orderSn);
                        orderTO.setNum(num);
                        orderTO.setSecKillPrice(skuRedisTO.getSecKillPrice());
                        orderTO.setPromotionSessionId(sessionId);
                        orderTO.setSkuId(skuId);
                        orderTO.setMemberId(user.getId());
                        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTO);
                        return orderSn;
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                        return null;
                    }
                }
            }
        }

        return null;
    }

    private void saveSessionInfo(List<SecKillSessionWithSkusVO> sessions) {
        for (SecKillSessionWithSkusVO session : sessions) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String key = SESSIONS_CACHE_PREFIX + session.getStartTime().getTime() + "-" + session.getEndTime().getTime();
            List<String> collect = session.getRelations()
                    .stream()
                    .map(e -> e.getPromotionSessionId() +"-" + e.getSkuId())
                    .collect(Collectors.toList());
            Boolean hasKey = redisTemplate.hasKey(key);
            if (!CollectionUtils.isEmpty(collect) && Boolean.FALSE.equals(hasKey)) {
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        }
    }

    private void saveSessionSkuInfo(List<SecKillSessionWithSkusVO> sessions) {
        for (SecKillSessionWithSkusVO session : sessions) {
            BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);
            for (SecKillSkuVO skuVO : session.getRelations()) {
                String skuKey = skuVO.getPromotionSessionId().toString() + "-" + skuVO.getSkuId();
                if (Boolean.FALSE.equals(ops.hasKey(skuKey))) {
                    SecKillSkuRedisTO secKillSkuRedisTO = new SecKillSkuRedisTO();
                    // sku 的基本数据
                    R r = productFeignService.getSkuInfo(skuVO.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVO skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVO>() {
                        });
                        secKillSkuRedisTO.setSkuInfoVO(skuInfo);
                    }
                    // sku 的秒杀信息
                    BeanUtils.copyProperties(skuVO, secKillSkuRedisTO);
                    secKillSkuRedisTO.setSecKillLimit(skuVO.getSecKillLimit());
                    secKillSkuRedisTO.setSecKillCount(skuVO.getSecKillCount());
                    // sku 的秒杀时间信息
                    secKillSkuRedisTO.setStartTime(session.getStartTime().getTime());
                    secKillSkuRedisTO.setEndTime(session.getEndTime().getTime());
                    // 秒杀随机码，保证公平性
                    String code = UUID.randomUUID().toString().replace("-", "");
                    secKillSkuRedisTO.setRandomCode(code);
                    String json = JSON.toJSONString(secKillSkuRedisTO);
                    log.info(json);
                    // 缓存
                    ops.put(skuKey, json);
                    // 使用商品的库存作为分布式信号量，限流
                    String semaphoreKey = SKU_STOCK_SEMAPHORE + code;
                    RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
                    semaphore.trySetPermits(skuVO.getSecKillCount());
                    log.info("秒杀商品上传");
                }

            }
        }
    }


}
