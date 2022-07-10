package com.guo.gulimall.order.web;


import com.guo.gulimall.order.service.OrderService;
import com.guo.gulimall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade() {

        OrderConfirmVO confirmVO = orderService.confirmOrder();
        return "confirm";
    }
}
