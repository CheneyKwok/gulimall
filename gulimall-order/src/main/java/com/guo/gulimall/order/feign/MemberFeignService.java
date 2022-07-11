package com.guo.gulimall.order.feign;

import com.guo.gulimall.order.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("member/memberreceiveaddress/{memberId}/address")
    List<MemberAddressVO> getAddress(@PathVariable("memberId") Long memberId);
}
