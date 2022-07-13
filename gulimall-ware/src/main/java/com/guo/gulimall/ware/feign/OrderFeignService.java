package com.guo.gulimall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @RequestMapping("order/order/status/{orderSn}")
    Integer getOrderStatus(@PathVariable("orderSn") String orderSn);
}
