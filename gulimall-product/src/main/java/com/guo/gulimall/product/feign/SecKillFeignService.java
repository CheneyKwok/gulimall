package com.guo.gulimall.product.feign;

import com.guo.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-seckill")
public interface SecKillFeignService {

    @GetMapping("/sku/secKill/{skuId}")
    R getSkuSecKillInfo(@PathVariable("skuId") Long skuId);
}
