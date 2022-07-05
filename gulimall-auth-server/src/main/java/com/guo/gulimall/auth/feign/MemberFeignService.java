package com.guo.gulimall.auth.feign;


import com.guo.common.utils.R;
import com.guo.gulimall.auth.vo.SocialUser;
import com.guo.gulimall.auth.vo.UserLoginVO;
import com.guo.gulimall.auth.vo.UserRegisterVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "gulimall-member")
public interface MemberFeignService {

    @RequestMapping("member/member/register")
    R register(@RequestBody UserRegisterVO registerVo);

    @RequestMapping("member/member/login")
    R login(@RequestBody UserLoginVO loginVo);

    @RequestMapping("member/member/oauth2/login")
    R login(@RequestBody SocialUser socialUser);
}
