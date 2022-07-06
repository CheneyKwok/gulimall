package com.guo.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url,
                            @CookieValue(value = "sso_token", required = false) String token,
                            Model model) {
        if (StringUtils.hasText(token)) {
            return "redirect:" + url + "?token=" + token;
        }
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("url") String url,
                        HttpServletResponse response) {

        // 登录成功
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            String token = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(token, username);
            Cookie cookie = new Cookie("sso_token", token);
            response.addCookie(cookie);
            // 将 cookie 保存在 sso-server 域名下，以后浏览器再次访问 sso-server 时，会再次带上 cookie，不用再次登录验证，直接验证 cookie
            return "redirect:" + url + "?token=" + token;
        } else {
            return "login";
        }
    }


    @GetMapping("/userInfo")
    @ResponseBody
    public String userInfo(@RequestParam("token") String token) {
         return redisTemplate.opsForValue().get(token);
    }
}
