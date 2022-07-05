package com.guo.gulimall.member.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialUser {

    /**
     * 社交用户ID
     */
    private String socialUid;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 昵称
     */
    private String name;
}
