package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * @author YuTian
 * @date 2019/4/25 9:05
 */
@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private UserClient userClient;
    public String authentication(String username, String password) {
        User user = userClient.getUserByUserNameandPassword(username, password);
        if (user==null){
            throw  new LyException(ExceptionEnum.USERNAME_PASSWORD_NOT_MATCH_ERROR);
        }
        try {
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()), prop.getPrivateKey(), prop.getExpire());
            return token;
        } catch (Exception e) {
            log.info("[授权中心] 生成token失败，用户名称:{}", username,e);
            throw  new LyException(ExceptionEnum.USER_TOKEN_CREATED_ERROR);

        }

    }
}
