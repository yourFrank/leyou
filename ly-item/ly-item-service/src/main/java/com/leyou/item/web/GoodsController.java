package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/8 8:16
 */
@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value="key",required = false) String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page ,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable
    ){

        return ResponseEntity.ok(goodsService.querySpuByPage(key,page,rows,saleable));

    }
    //插入商品
    @PostMapping
    public ResponseEntity<Void> insertGoods(@RequestBody Spu spu){
        goodsService.insertGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //商品回显
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long id) {
        List<Sku> skus = this.goodsService.querySkuBySpuId(id);
        if (skus == null || skus.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(skus);
    }

    //商品回显
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id") Long id) {
        SpuDetail detail = this.goodsService.querySpuDetailById(id);
        if (detail == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(detail);
    }
    //商品更新
    @PutMapping
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu) {
        try {
            this.goodsService.updateGoods(spu);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //商品下架
    @PutMapping("spu/downgood/{id}")
       public ResponseEntity<Void> downGood(@PathVariable("id") Long id){
        goodsService.downGood(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //商品上架
    @PutMapping("spu/upgood/{id}")
    public ResponseEntity<Void> upGood(@PathVariable("id") Long id){
        goodsService.upGood(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("spu/{id}")
        public ResponseEntity<Void> deleteGoods(@PathVariable("id") Long id){
            goodsService.deleteGoods(id);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id")Long id){
        Sku sku = this.goodsService.querySkuById(id);
        if (sku == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(sku);
    }
}


