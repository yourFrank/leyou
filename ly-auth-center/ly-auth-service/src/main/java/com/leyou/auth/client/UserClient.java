package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author YuTian
 * @date 2019/4/25 8:59
 */
@FeignClient(value = "user-service")
public interface UserClient extends UserApi {
}
