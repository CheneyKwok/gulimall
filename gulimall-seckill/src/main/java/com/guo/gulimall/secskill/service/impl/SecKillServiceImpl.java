package com.guo.gulimall.secskill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.guo.common.utils.R;
import com.guo.gulimall.secskill.feign.CouponFeignService;
import com.guo.gulimall.secskill.feign.ProductFeignService;
import com.guo.gulimall.secskill.service.SecKillService;
import com.guo.gulimall.secskill.to.SecKillSkuRedisTO;
import com.guo.gulimall.secskill.vo.SecKillSessionWithSkusVO;
import com.guo.gulimall.secskill.vo.SecKillSkuVO;
import com.guo.gulimall.secskill.vo.SkuInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);
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
                    // sku 的秒杀时间信息
                    secKillSkuRedisTO.setStartTime(session.getStartTime().getTime());
                    secKillSkuRedisTO.setEndTime(session.getEndTime().getTime());
                    // 秒杀随机码，保证公平性
                    String code = UUID.randomUUID().toString().replace("-", "");
                    secKillSkuRedisTO.setRandomCode(code);
                    // 缓存
                    ops.put(skuKey, JSON.toJSONString(secKillSkuRedisTO));
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
