package com.guo.gulimall.order.feign;


import com.guo.gulimall.order.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService{

    @GetMapping("/currentUserCartItems")
    List<OrderItemVO> getCurrentUserCartItems();
}
