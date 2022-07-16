package com.guo.gulimall.member;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberWebController {

    @GetMapping("/memberOrder")
    public String memberOrderPage() {
        return "orderList";
    }
}
