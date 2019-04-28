package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author YuTian
 * @date 2019/4/25 8:56
 */
public interface UserApi  {

    @GetMapping("/query")
    public User getUserByUserNameandPassword(@RequestParam("username") String username,
                                      @RequestParam("password") String password);

}
