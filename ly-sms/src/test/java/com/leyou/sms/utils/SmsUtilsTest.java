package com.leyou.sms.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author YuTian
 * @date 2019/4/23 13:53
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SmsUtilsTest {
    @Autowired
    private AmqpTemplate template;

    @Test
    public void test() throws InterruptedException {
        Map<String,String> map=new HashMap<>();
        map.put("phone","17865426196");
        map.put("code","54321");
        template.convertAndSend("ly.sms.exchange","sms.verify.code",map);
        Thread.sleep(10000L);
    }

}