package com.guo.gulimall.cart.vo;


import lombok.Data;

@Data
public class UserInfoVO {

    private Long userId;

    private String userKey;

    private Boolean findUserKey = false;
}
