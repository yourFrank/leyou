package com.leyou.gateway.filter;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/25 13:08
 */
@Component
@EnableConfigurationProperties({FilterProperties.class,JwtProperties.class})
public class LoginFilter extends ZuulFilter {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private FilterProperties filterProperties;
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;//过滤类型
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER-1; //过滤的顺序
    }

    //是否过滤
    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取URI（出去域名和端口之后的）
        HttpServletRequest request = currentContext.getRequest();
        String requestURI = request.getRequestURI();
        //判断是否在路径中
        Boolean isAllowed=isAllowed(requestURI);
        //判断如果不在白名单内要进行拦截
        return !isAllowed;
    }

    private Boolean isAllowed(String requestURI) {
        //获取允许的白名单
        List<String> allowPaths = filterProperties.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (requestURI.startsWith(allowPath)){
                return true;
            }
        }
            return false;
    }

    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //工具类获取cookie，直接获得token。帮我们遍历
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            JwtUtils.getInfoFromToken(token,jwtProperties.getPublicKey());
            // TODO 校验身份
        } catch (Exception e) {
            //解析token失败，拦截
            context.setSendZuulResponse(false);
            //返回状态码
            context.setResponseStatusCode(403);
        }

             return null;//默认setSendZuulResponse为true放心
    }
}
