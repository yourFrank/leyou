package com.leyou.search.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author YuTian
 * @date 2019/4/13 17:42
 */
@FeignClient("item-service")
public interface BrandClient extends BrandApi {
}
