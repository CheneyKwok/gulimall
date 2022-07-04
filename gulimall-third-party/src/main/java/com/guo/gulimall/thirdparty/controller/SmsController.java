package com.guo.gulimall.thirdparty.controller;

import com.guo.common.utils.R;
import com.guo.gulimall.thirdparty.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SmsController {


    @Autowired
    SmsService smsService;


    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {

        smsService.sendSmsCode(phone, code);
        return R.ok();
    }
}
