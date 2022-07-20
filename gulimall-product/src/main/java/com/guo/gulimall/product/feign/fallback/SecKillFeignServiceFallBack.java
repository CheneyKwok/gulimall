package com.guo.gulimall.product.feign.fallback;

import com.guo.common.excepiton.BizCodeEnum;
import com.guo.common.utils.R;
import com.guo.gulimall.product.feign.SecKillFeignService;
import org.springframework.stereotype.Service;


@Service
public class SecKillFeignServiceFallBack implements SecKillFeignService {
    @Override
    public R getSkuSecKillInfo(Long skuId) {
        BizCodeEnum codeEnum = BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION;
        return R.error(codeEnum.getCode(), codeEnum.getMsg());
    }
}
