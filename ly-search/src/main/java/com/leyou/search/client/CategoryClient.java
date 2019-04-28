package com.leyou.search.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author YuTian
 * @date 2019/4/13 16:15
 */
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {


}
