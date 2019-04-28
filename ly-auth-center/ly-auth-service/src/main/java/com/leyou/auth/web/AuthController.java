package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author YuTian
 * @date 2019/4/25 9:04
 */
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties prop;
    @Value("${ly.jwt.cookieName}")
    private String cookieName;

    @PostMapping("login")
    public ResponseEntity<Void> authentication(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response,
            HttpServletRequest request){
        String token=authService.authentication(username,password);

        CookieUtils.newBuilder(response).httpOnly().request(request).build(cookieName,token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyByToken(@CookieValue("LY_TOKEN") String token,
                                                  HttpServletResponse response,
                                                  HttpServletRequest request) {
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            String newToken = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            CookieUtils.newBuilder(response).httpOnly().request(request).build(cookieName,token);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            throw  new LyException(ExceptionEnum.UNAUTHORIZED);
        }



    }
}
