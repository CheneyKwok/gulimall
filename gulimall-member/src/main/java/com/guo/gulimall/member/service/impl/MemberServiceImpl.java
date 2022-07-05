package com.guo.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.gulimall.member.dao.MemberDao;
import com.guo.gulimall.member.entity.MemberEntity;
import com.guo.gulimall.member.entity.MemberLevelEntity;
import com.guo.gulimall.member.exception.PhoneNumExistException;
import com.guo.gulimall.member.exception.UserExistException;
import com.guo.gulimall.member.service.MemberLevelService;
import com.guo.gulimall.member.service.MemberService;
import com.guo.gulimall.member.vo.MemberLoginVO;
import com.guo.gulimall.member.vo.MemberRegisterVO;
import com.guo.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVO registerVo) {

        //1 检查电话号是否唯一
        checkPhoneUnique(registerVo.getPhone());
        //2 检查用户名是否唯一
        checkUserNameUnique(registerVo.getUsername());
        //3 该用户信息唯一，进行插入
        MemberEntity entity = new MemberEntity();
        //3.1 保存基本信息
        entity.setUsername(registerVo.getUsername());
        entity.setMobile(registerVo.getPhone());
        entity.setCreateTime(new Date());
        //3.2 使用加密保存密码
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(registerVo.getPassword());
        entity.setPassword(encodePassword);
        //3.3 设置会员默认等级
        //3.3.1 找到会员默认登记
        MemberLevelEntity memberLevelEntity = memberLevelService.getDefaultLevel();
        //3.3.2 设置会员等级为默认
        entity.setLevelId(memberLevelEntity.getId());

        // 4 保存用户信息
        this.save(entity);
    }

    @Override
    public MemberEntity login(MemberLoginVO memberLoginVO) {
        String loginAccount = memberLoginVO.getLoginAccount();
        //以用户名或电话号登录的进行查询
        MemberEntity entity = lambdaQuery().eq(MemberEntity::getUsername, loginAccount).or().eq(MemberEntity::getMobile, loginAccount).one();
        if (entity!=null){
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(memberLoginVO.getPassword(), entity.getPassword());
            if (matches){
                entity.setPassword("");
                return entity;
            }
        }
        return null;
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        MemberEntity member = lambdaQuery().eq(MemberEntity::getSocialUid, socialUser.getSocialUid()).one();
        if (member == null) {
            // 未登录过则进行注册
            member = MemberEntity.builder()
                    .levelId(memberLevelService.getDefaultLevel().getId())
                    .nickname(socialUser.getName())
                    .header(socialUser.getAvatarUrl())
                    .accessToken(socialUser.getAccessToken())
                    .socialUid(socialUser.getSocialUid())
                    .build();
            save(member);
        } else {
            member.setNickname(socialUser.getName());
            member.setAccessToken(socialUser.getAccessToken());
            member.setHeader(socialUser.getAvatarUrl());
            updateById(member);
        }
        return member;
    }

    private void checkUserNameUnique(String userName) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count > 0) {
            throw new UserExistException();
        }
    }

    private void checkPhoneUnique(String phone) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneNumExistException();
        }
    }

}