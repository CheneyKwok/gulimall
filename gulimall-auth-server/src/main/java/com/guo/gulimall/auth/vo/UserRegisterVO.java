package com.guo.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterVO {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6, max = 18, message = "长度必须是6至18个字符")
    private String username;
    @NotEmpty(message = "密码必须提交")
    @Length(min = 6, max = 18, message = "长度必须是6至18个字符")
    private String password;
    @Pattern(regexp = "^[1][3-9][0-9]{9}$", message = "手机号格式不正确")
    @NotEmpty(message = "手机号不能为空")
    private String phone;
    @NotEmpty(message = "验证码不能为空")
    private String code;
}
