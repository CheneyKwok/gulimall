package com.guo.gulimall.auth.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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
