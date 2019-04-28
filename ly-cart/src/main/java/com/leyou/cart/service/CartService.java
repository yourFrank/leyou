package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuTian
 * @date 2019/4/26 12:38
 */
@Service
public class CartService {

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String key_prefix = "ly:cart:uid:";

    public void addCart(Cart cart) {
        //获取用户信息
        UserInfo loginUser = LoginInterceptor.getLoginUser();

        //根据userId取值
        BoundHashOperations<String, Object, Object> map = redisTemplate.boundHashOps(key_prefix + loginUser.getId());
        //判断如果存在进一步判断
        String key = cart.getSkuId().toString();
        if (map.hasKey(key)) {
            //获取购物车项
            String json = map.get(key).toString();
            Cart cartItem = JsonUtils.toBean(json, Cart.class);
            cart.setNum(cartItem.getNum() + cart.getNum());
        } else {
            //如果不存在
            //根据skuid查询完整的sku信息
            Sku sku = goodsClient.querySkuById(cart.getSkuId());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : sku.getImages().split(",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setUserId(loginUser.getId());
        }
        //最后将购物车写回reids(有则覆盖，没有则添加)
        map.put(key, JsonUtils.toString(cart));
    }

    public List<Cart> getCart() {
        //获取用户信息
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        //获取redis中的购物车项
        if (!redisTemplate.hasKey(key_prefix + loginUser.getId())) {
            return null;
        }
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(key_prefix + loginUser.getId());

        //将购物车的项转为List返回
        List<Object> values = hashOperations.values();
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.stream().map(value -> JsonUtils.toBean(value.toString(), Cart.class)).collect(Collectors.toList());

    }

    public void updateNum(Cart cart) {
        //获取用户信息
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        if (!redisTemplate.hasKey(key_prefix + loginUser.getId())) {
            return;
        }
        //获取redis中的购物车项
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(key_prefix + loginUser.getId());
        //根据skuid直接操作num值
        String cartItem = hashOperations.get(cart.getSkuId().toString()).toString();
        //使用jsonUtil转为cart对象
        Cart redisCart = JsonUtils.toBean(cartItem, Cart.class);
        redisCart.setNum(cart.getNum());
        hashOperations.put(cart.getSkuId().toString(), JsonUtils.toString(redisCart));

    }

    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        String key = key_prefix + user.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }

    public void addLocal(List<Cart> cart) {
        //获取用户信息
        UserInfo loginUser = LoginInterceptor.getLoginUser();

        //根据userId取值
        BoundHashOperations<String, Object, Object> map = redisTemplate.boundHashOps(key_prefix + loginUser.getId());
        for (Cart cart1 : cart) {
            addCart(cart1);
        }
    }
}
