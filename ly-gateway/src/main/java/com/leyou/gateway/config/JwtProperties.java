package com.leyou.gateway.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @author YuTian
 * @date 2019/4/24 16:24
 */
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {


    private String pubKeyPath;// 公钥路径

    private PublicKey publicKey; // 公钥

    private String cookieName;//cookieName

    /**
     * @PostContruct：在构造方法执行之后执行该方法
     */
    @PostConstruct
    public void init() throws Exception {

        //获取公钥和私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}
