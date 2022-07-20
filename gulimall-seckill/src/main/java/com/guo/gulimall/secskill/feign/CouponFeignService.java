package com.guo.gulimall.secskill.feign;

import com.guo.common.utils.R;
import com.guo.gulimall.secskill.feign.fallback.CouponFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "gulimall-coupon", fallback = CouponFeignServiceFallBack.class)
public interface CouponFeignService {


    @PostMapping("coupon/seckillsession/getSecKillSessionsIn3Days")
    R getSecKillSessionsIn3Days();
}