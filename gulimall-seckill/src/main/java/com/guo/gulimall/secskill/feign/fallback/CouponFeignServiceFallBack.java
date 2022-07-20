package com.guo.gulimall.secskill.feign.fallback;

import com.guo.common.excepiton.BizCodeEnum;
import com.guo.common.utils.R;
import com.guo.gulimall.secskill.feign.CouponFeignService;
import org.springframework.stereotype.Service;


@Service
public class CouponFeignServiceFallBack implements CouponFeignService {
    @Override
    public R getSecKillSessionsIn3Days() {
        BizCodeEnum codeEnum = BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION;
        return R.error(codeEnum.getCode(), codeEnum.getMsg());
    }
}
