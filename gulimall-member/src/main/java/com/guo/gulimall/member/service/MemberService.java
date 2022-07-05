package com.guo.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.member.entity.MemberEntity;
import com.guo.gulimall.member.vo.MemberLoginVO;
import com.guo.gulimall.member.vo.MemberRegisterVO;
import com.guo.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:11:26
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVO registerVo);

    MemberEntity login(MemberLoginVO memberLoginVO);

    MemberEntity login(SocialUser socialUser);
}

