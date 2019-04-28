package com.leyou.auth.utils;

import com.leyou.auth.pojo.UserInfo;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author YuTian
 * @date 2019/4/24 16:12
 */


public class JwtUtilsTest {

    private static final String pubKeyPath = "G:\\代码\\rsa\\rsa.pub";

    private static final String priKeyPath = "G:\\代码\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU1NjA5NDIwNX0.CiOI4E8Sqn2_3gU9MRluANZoPCW7tFumNcte5V9HE2icboEIAjU33SDJyaxCEF0LA4xReq2mpp96wjprc76lfETotvC243FznqbQsCTMc1V1JeV1IQO5fbPce99sBbUS3xFmrIJR_b7FI70Chi6ENFu9tmBkkzaWvDxZe7npcU0";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}