package com.guo.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.guo.common.constant.AuthConstant;
import com.guo.common.utils.R;
import com.guo.common.vo.MemberRespVO;
import com.guo.gulimall.auth.feign.MemberFeignService;
import com.guo.gulimall.auth.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            return "login";
        } else {
            return "redirect:http://gulimall.com";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVO loginVO, RedirectAttributes attributes, HttpSession session){
        R r = memberFeignService.login(loginVO);
        if (r.getCode() == 0) {
            MemberRespVO memberRespVO = r.getData("member", new TypeReference<MemberRespVO>() {
            });
            session.setAttribute(AuthConstant.LOGIN_USER, memberRespVO);
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
