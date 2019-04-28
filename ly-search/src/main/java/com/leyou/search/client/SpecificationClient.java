package com.leyou.search.client;

import com.leyou.item.api.SpecificationApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author YuTian
 * @date 2019/4/13 17:43
 */
@FeignClient("item-service")
public interface SpecificationClient extends SpecificationApi {
}
