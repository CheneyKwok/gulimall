package com.guo.gulimall.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.guo.common.utils.R;
import com.guo.gulimall.auth.feign.MemberFeignService;
import com.guo.gulimall.auth.vo.MemberRespVO;
import com.guo.gulimall.auth.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class OAuth2Controller {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth/github")
    public String githubOAuth(@RequestParam("code") String code) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", "6d7be6c661d885288eea");
        params.put("client_secret", "97bb4839f4ffdcd1195252490b99608ffa8ad127");
        params.put("code", code);
        System.out.println("code = " + code);
        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity("https://github.com/login/oauth/access_token", params, JSONObject.class);
        JSONObject body;
        if (responseEntity.getStatusCode() == HttpStatus.OK && (body = responseEntity.getBody())!= null) {
            String access_token = body.getString("access_token");
            System.out.println("access_token = " + access_token);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Token " + access_token);
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            responseEntity = restTemplate.postForEntity("https://api.github.com/user", entity, JSONObject.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK && (body = responseEntity.getBody())!= null) {
                String id = body.getString("id");
                String name = body.getString("login");
                String avatarUrl = body.getString("avatar_url");
                SocialUser socialUser = new SocialUser();
                socialUser.setSocialUid(id);
                socialUser.setAccessToken(access_token);
                socialUser.setName(name);
                socialUser.setAvatarUrl(avatarUrl);
                R r = memberFeignService.login(socialUser);
                if (r.getCode() == 0) {
                    MemberRespVO memberRespVO = r.getData("member", new TypeReference<MemberRespVO>() {
                    });
                    System.out.println("memberRespVO = " + memberRespVO.toString());
                }
            }
                return "redirect:http://gulimall.com";
        }
        return "redirect://http://auth.gulimall.com/login,html";
    }

    @GetMapping("/oauth/gitee")
    public String giteeOAuth(@RequestParam("code") String code) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", "5e9c1e6ab6fd6d7b4b514ff166c46717fcb99062318d4c535fef65c4d939beca");
        params.put("client_secret", "6eb8af184489a1b07e1583fbd815744ffa276002cd970531fb568a3fdc5718ac");
        params.put("code", code);
        System.out.println("code = " + code);
        params.put("redirect_uri", "http://auth.gulimall.com/oauth/gitee");
        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity("https://gitee.com/oauth/token?grant_type=authorization_code", params, JSONObject.class);
        JSONObject body;
        if (responseEntity.getStatusCode() == HttpStatus.OK && (body = responseEntity.getBody())!= null) {
            String access_token = body.getString("access_token");
            System.out.println("access_token = " + access_token);
            responseEntity = restTemplate.getForEntity("https://gitee.com/api/v5/user?access_token={access_token}", JSONObject.class, access_token);
            if (responseEntity.getStatusCode() == HttpStatus.OK && (body = responseEntity.getBody())!= null) {
                String id = body.getString("id");
                String name = body.getString("login");
                String avatarUrl = body.getString("avatar_url");
                SocialUser socialUser = new SocialUser();
                socialUser.setSocialUid(id);
                socialUser.setAccessToken(access_token);
                socialUser.setName(name);
                socialUser.setAvatarUrl(avatarUrl);
                R r = memberFeignService.login(socialUser);
                if (r.getCode() == 0) {
                    MemberRespVO memberRespVO = r.getData("member", new TypeReference<MemberRespVO>() {
                    });
                    System.out.println("memberRespVO = " + memberRespVO.toString());
                }
            }
            return "redirect:http://gulimall.com";
        }
        return "redirect://http://auth.gulimall.com/login,html";
    }
}
