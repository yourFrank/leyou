package com.leyou.sms.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author YuTian
 * @date 2019/4/23 11:44
 */
@Slf4j
 @Component
 @EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils {

     @Autowired
     private SmsProperties prop;
     @Autowired
     private StringRedisTemplate redisTemplate;

    //产品名称:云通信短信API产品,开发者无需替换
    static final String product = "Dysmsapi";
    //产品域名,开发者无需替换
    static final String domain = "dysmsapi.aliyuncs.com";
    //redis sms key前缀
    private  static  final String KEY_PREFIX="sms:phone:";
    //最小时间间隔 60s
    private static  final long SMS_MIN_INTERVAL_IN_MILLIS=60000;
    public  SendSmsResponse sendSms(String phoneNumber,String singName,String templateCode,String templateParam)  {

        //手机号码限流
        String key=KEY_PREFIX+phoneNumber;
        String s = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(s)) {
            if (System.currentTimeMillis() - Long.valueOf(s) < SMS_MIN_INTERVAL_IN_MILLIS) {
                log.info("[短信服务] 发送频率过高，被拦截，手机号码:{}" ,phoneNumber);
                return null;
            }
        }
        try {

            //可自助调整超时时间
            System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
            System.setProperty("sun.net.client.defaultReadTimeout", "10000");

            //初始化acsClient,暂不支持region化
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", prop.getAccessKeyId(), prop.getAccessKeySecret());
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
            IAcsClient acsClient = new DefaultAcsClient(profile);

            //组装请求对象-具体描述见控制台-文档部分内容
            SendSmsRequest request = new SendSmsRequest();
            request.setMethod(MethodType.POST);
            //必填:待发送手机号
            request.setPhoneNumbers(phoneNumber);
            //必填:短信签名-可在短信控制台中找到
            request.setSignName(singName);
            //必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(templateCode);
            //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
            request.setTemplateParam(templateParam);

            //hint 此处可能会抛出异常，注意catch
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

            if (!"OK".equals(sendSmsResponse.getCode())) {
                log.info("[短信服务] 发送短信失败, phoneNumber:{}，原因:{}",phoneNumber,sendSmsResponse.getMessage());
            }
            redisTemplate.opsForValue().set(key,String.valueOf(System.currentTimeMillis()),1,TimeUnit.MINUTES);
            return sendSmsResponse;
        }
        catch (Exception e){
            log.error("[短信服务] 发送短信异常,手机号码:{}",phoneNumber,e);
            return null;
        }

        }


}
