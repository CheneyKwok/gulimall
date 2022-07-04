package com.guo.gulimall.auth.controller;

import com.guo.common.utils.R;
import com.guo.gulimall.auth.feign.MemberFeignService;
import com.guo.gulimall.auth.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private MemberFeignService memberFeignService;

    @PostMapping("/login")
    public String login(UserLoginVO loginVO, RedirectAttributes attributes, HttpSession session){
        R r = memberFeignService.login(loginVO);
        if (r.getCode() == 0) {
            return "redirect:http://gulimall.com/";
        }else {
            String msg = (String) r.get("msg");
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", msg);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
