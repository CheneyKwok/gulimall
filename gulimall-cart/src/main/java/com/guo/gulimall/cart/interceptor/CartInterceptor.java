package com.guo.gulimall.cart.interceptor;

import com.guo.common.constant.AuthConstant;
import com.guo.common.constant.CartConstant;
import com.guo.common.vo.MemberRespVO;
import com.guo.gulimall.cart.vo.UserInfoVO;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 用户登录状态拦截器
 *
 * 已登录：从 session 中获取用户数据
 * 未登录：从 cookie 中获取临时 user-key
 */

public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoVO> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoVO userInfoVO = new UserInfoVO();
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute instanceof MemberRespVO) {
            MemberRespVO memberRespVO = (MemberRespVO) attribute;
            userInfoVO.setUserId(memberRespVO.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoVO.setUserKey(cookie.getValue());
                    userInfoVO.setFindUserKey(true);
                    break;
                }
            }
        }
        if (!userInfoVO.getFindUserKey()) {
            String userKey = UUID.randomUUID().toString();
            userInfoVO.setUserKey(userKey);
        }
        threadLocal.set(userInfoVO);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoVO userInfoVO = threadLocal.get();
        if (!userInfoVO.getFindUserKey()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoVO.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIME_OUT);
            response.addCookie(cookie);
        }
    }
}
