package com.guo.gulimall.auth.controller;


import com.guo.common.constant.AuthConstant;
import com.guo.common.excepiton.BizCodeEnum;
import com.guo.common.utils.R;
import com.guo.gulimall.auth.feign.MemberFeignService;
import com.guo.gulimall.auth.feign.ThirdPartFeignService;
import com.guo.gulimall.auth.vo.UserRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
public class RegController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @PostMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(String phone) {
        if (phone.isEmpty()) {
            return R.error();
        }
        String codeKey = AuthConstant.SMS_CODE_CACHE_PREFIX + phone;

        // 验证码再次校验
        String code = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.isEmpty(code)) {
            String[] strings = code.split("_");
            long sendTime = Long.parseLong(strings[1]);
            if (System.currentTimeMillis() - sendTime < 60 * 1000) {
                BizCodeEnum smsCodeEnum = BizCodeEnum.SMS_CODE_EXCEPTION;
                return R.error(smsCodeEnum.getCode(), smsCodeEnum.getMsg());
            }
        }
        // todo 接口防刷
        code = generateCode(4);
        System.out.println("code =" + code);
        stringRedisTemplate.opsForValue().set(codeKey, code + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);
//        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVO registerVo, BindingResult result, RedirectAttributes attributes) {
        //1.判断校验是否通过
        Map<String, String> errors = new HashMap<>();
        if (result.hasErrors()){
            //1.1 如果校验不通过，则封装校验结果
            result.getFieldErrors().forEach(item->{
                errors.put(item.getField(), item.getDefaultMessage());
                //1.2 将错误信息封装到session中
                attributes.addFlashAttribute("errors", errors);
            });
            //1.2 重定向到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }else {
            //2.若JSR303校验通过
            //判断验证码是否正确
            String code = stringRedisTemplate.opsForValue().get(AuthConstant .SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
            //2.1 如果对应手机的验证码不为空且与提交上的相等-》验证码正确
            if (!StringUtils.isEmpty(code) && registerVo.getCode().equals(code.split("_")[0])) {
                //2.1.1 使得验证后的验证码失效
                stringRedisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());

                //2.1.2 远程调用会员服务注册
                R r = memberFeignService.register(registerVo);
                if (r.getCode() == 0) {
                    //调用成功，重定向登录页
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    //调用失败，返回注册页并显示错误信息
                    String msg = (String) r.get("msg");
                    errors.put("msg", msg);
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else {
                //2.2 验证码错误
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }
    }

    public static String generateCode(int count) {
        List<Integer> set = getRandomNumber(count);
        // 使用迭代器
        Iterator<Integer> iterator = set.iterator();
        // 临时记录数据
        StringBuilder temp = new StringBuilder();
        while (iterator.hasNext()) {
            temp.append(iterator.next());

        }
        return temp.toString();
    }

    public static List<Integer> getRandomNumber(int count) {
        List<Integer> set = new ArrayList<>();
        // 随机数
        Random random = new Random();

        while (set.size() < count) {
            set.add(random.nextInt(10));
        }
        return set;
    }


}
