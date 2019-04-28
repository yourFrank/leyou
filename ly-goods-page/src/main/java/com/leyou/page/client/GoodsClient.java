package com.leyou.page.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author YuTian
 * @date 2019/4/13 17:41
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
