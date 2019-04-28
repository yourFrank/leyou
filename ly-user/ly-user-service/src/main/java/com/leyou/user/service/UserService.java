package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YuTian
 * @date 2019/4/23 16:13
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX ="user:verify:phone:";

    public Boolean checkData(String data, Integer type) {
        User user=new User();
        switch (type){
            case 1:user.setUsername(data);
            break;
            case 2:user.setPhone(data);
            break;
            default:throw new LyException(ExceptionEnum.INVALID_USER_TYPE_ERROR);
        }
        return userMapper.selectCount(user)==0;

    }



    public void generateCode(String phone) {

        Map<String,String> map=new HashMap<>();
        map.put("phone",phone);
        //生成验证码
        String code = NumberUtils.generateCode(6);
        map.put("code",code);
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",map);
        //保存验证码，方便校验
        String key= KEY_PREFIX +phone;
        redisTemplate.opsForValue().set(key,code,5,TimeUnit.MINUTES);
    }

    public void registUser(User user, String code) {
        //从redis中获取code
        String codeCache=redisTemplate.opsForValue().get(KEY_PREFIX+user.getPhone());
        //使用util比较，可以校验空值
        if (!StringUtils.equals(codeCache,code)){
            throw  new LyException(ExceptionEnum.INVALID_PHONE_CODE_ERROR);
        }
        //生成盐值
        String salt = CodecUtils.generateSalt();
        //使用工具类加密
        String newPassword = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setSalt(salt);
        user.setPassword(newPassword);
        user.setCreated(new Date());
        userMapper.insert(user);

    }

    public User getUserByUserNameandPassword(String username, String password) {
        User user=new User();
        //数据库username字段为索引字段，直接通过username查可以增加效率
        user.setUsername(username);
        User databaseUser = userMapper.selectOne(user);
        if (databaseUser==null){
            throw  new LyException(ExceptionEnum.USERNAME_PASSWORD_NOT_MATCH_ERROR);

        }
        if (!StringUtils.equals(CodecUtils.md5Hex(password,databaseUser.getSalt()),databaseUser.getPassword())){
            throw  new LyException(ExceptionEnum.USERNAME_PASSWORD_NOT_MATCH_ERROR);
        }
        //用户名密码都正确
        return databaseUser;
    }
}
