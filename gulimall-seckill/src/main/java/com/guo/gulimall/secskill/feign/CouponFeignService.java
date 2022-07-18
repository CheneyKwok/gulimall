package com.guo.gulimall.secskill.feign;

import com.guo.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "gulimall-coupon")
public interface CouponFeignService {


    @PostMapping("coupon/seckillsession/getSecKillSessionsIn3Days")
    R getSecKillSessionsIn3Days();
}