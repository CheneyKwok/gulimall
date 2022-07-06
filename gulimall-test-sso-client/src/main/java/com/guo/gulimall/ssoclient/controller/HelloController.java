package com.guo.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {

    @Value("${sso-server-url}")

    private String ssoServerUrl;


    @Autowired
    private RestTemplate restTemplate;

    /**
     * 无需登录就可访问
     * @return
     */
    @GetMapping("/hello")
    @ResponseBody
    public String hello() {

        return "hello";
    }


    @GetMapping("/employees")
    public String employees(Model model, HttpSession session, @RequestParam(value = "token", required = false) String token) {
        // 携带 token 说明从认证中心认证
        if (StringUtils.hasText(token)) {
            // 去认证中心查询用户信息
            ResponseEntity<String> responseEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token={token}", String.class, token);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String username = responseEntity.getBody();
                session.setAttribute("loginUser", username);
            }
        }
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client1.com:8081/employees";
        }
        List<String> list = new ArrayList<>();
        list.add("张三");
        list.add("李四");
        model.addAttribute("emps", list);
        return "index";
    }
}
