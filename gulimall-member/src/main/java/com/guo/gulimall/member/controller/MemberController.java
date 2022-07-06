package com.guo.gulimall.member.controller;

import com.guo.common.excepiton.BizCodeEnum;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.R;
import com.guo.gulimall.member.entity.MemberEntity;
import com.guo.gulimall.member.exception.PhoneNumExistException;
import com.guo.gulimall.member.exception.UserExistException;
import com.guo.gulimall.member.feign.CouponFeignService;
import com.guo.gulimall.member.service.MemberService;
import com.guo.gulimall.member.vo.MemberLoginVO;
import com.guo.gulimall.member.vo.MemberRegisterVO;
import com.guo.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:11:26
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private CouponFeignService couponFeignService;


    @RequestMapping("/login")
    public R login(@RequestBody MemberLoginVO memberLoginVO) {
        MemberEntity entity = memberService.login(memberLoginVO);
        if (entity != null) {
            return R.ok().put("member", entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCT_PASSWORD_EXCEPTION.getMsg());
        }
    }

    @RequestMapping("/oauth2/login")
    public R login(@RequestBody SocialUser socialUser) {
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            return R.ok().put("member", entity);
        } else {
            return R.error();
        }
    }

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));

    }

    /**
     * 注册会员
     *
     * @return
     */
    @RequestMapping("/register")
    public R register(@RequestBody MemberRegisterVO registerVo) {
        try {
            memberService.register(registerVo);
        } catch (UserExistException userException) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneNumExistException phoneException) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
