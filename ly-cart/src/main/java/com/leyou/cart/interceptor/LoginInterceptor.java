package com.leyou.cart.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author YuTian
 * @date 2019/4/26 12:40
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties prop;
    // 定义一个线程域，存放登录用户
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        //因为前面做了判断，所以这里不会为空，直接解析token
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        if (userInfo==null){
            return false;
        }
        //将信息存入一个threadlocal中，方便后面的获取
        tl.set(userInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //由于使用的tomcat的线程池，要将threadlocal清空，防止后面的人拿到有值的
        tl.remove();
    }

    //由于将threadlocal私有化，这里使用静态方法调用获得user
    public static UserInfo getLoginUser() {
        return tl.get();
    }
}
