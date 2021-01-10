package com.guo.gulimall.product.feign;

import com.guo.common.to.SkuReductionTo;
import com.guo.common.to.SpuBoundTo;
import com.guo.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 1、@RequestBody 将 SpuBoundTo 对象转为json
     * 2、找到gulimall-coupon服务，将json数据放在请求体里发送coupon/spubounds/save请求
     * 2、接受到请求体里的json数据,@RequestBody 将 json 数据转为接受的对象类型对象
     */
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(SkuReductionTo skuReductionTo);

}
