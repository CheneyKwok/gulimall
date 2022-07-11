package com.guo.gulimall.ware.feign;


import com.guo.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("member/memberreceiveaddress/info/{id}")
    R infoAddress(@PathVariable("id") Long id);
}
