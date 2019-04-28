package com.leyou.cart.web;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/26 12:37
 */
@RestController
@Service
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public ResponseEntity<List<Cart>> getCart(){
        List<Cart> carts=cartService.getCart();
        if (CollectionUtils.isEmpty(carts)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carts);
    }
    @PutMapping
    public  ResponseEntity<Void> updateNum(@RequestBody Cart cart){
        cartService.updateNum(cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    /**
     * 合并localstorage到redis
     * @param cart
     * @return
     */
    @PostMapping("addLocal")
    public ResponseEntity<Void> addLocal(@RequestBody List<Cart> cart) {
        this.cartService.addLocal(cart);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
}
