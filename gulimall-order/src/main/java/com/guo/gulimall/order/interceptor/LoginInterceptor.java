package com.guo.gulimall.order.interceptor;

import com.guo.common.constant.AuthConstant;
import com.guo.common.vo.MemberRespVO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberRespVO> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Object attribute = request.getSession().getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        loginUser.set((MemberRespVO) attribute);
        return true;
    }
}
