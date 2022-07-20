package com.guo.gulimall.product.feign;

import com.guo.common.utils.R;
import com.guo.gulimall.product.feign.fallback.SecKillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "gulimall-seckill", fallback = SecKillFeignServiceFallBack.class)
public interface SecKillFeignService {

    @GetMapping("/sku/secKill/{skuId}")
    R getSkuSecKillInfo(@PathVariable("skuId") Long skuId);
}
