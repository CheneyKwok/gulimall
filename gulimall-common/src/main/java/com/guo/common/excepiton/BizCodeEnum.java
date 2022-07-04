package com.guo.common.excepiton;

/**
 * 错误码列表
 *
 * 10： 通用
 * 11： 商品
 * 12： 订单
 * 13： 购物车
 * 14： 物流
 */
public enum BizCodeEnum {
    UN_KNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率过高"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"存在相同的用户"),
    PHONE_EXIST_EXCEPTION(15002, "存在相同的手机号"),
    LOGIN_ACCT_PASSWORD_EXCEPTION(15003,"账号或密码错误");

    private final int code;
    private final String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
