package com.leyou.cart.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author YuTian
 * @date 2019/4/26 13:28
 */
@FeignClient("item-service")
public interface GoodsClient  extends  GoodsApi {
}
