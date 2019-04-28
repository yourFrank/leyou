package com.leyou.item.api;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/13 17:31
 */
public interface GoodsApi {

    @GetMapping("spu/page")
   PageResult<Spu> querySpuByPage(
            @RequestParam(value="key",required = false) String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page ,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable
    );

    @GetMapping("/spu/detail/{id}")
     SpuDetail querySpuDetailById(@PathVariable("id") Long id) ;
    //商品回显
    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id) ;

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
   Spu querySpuById(@PathVariable("id") Long id);

    /**
     * 根据skuid查询sku
     * @param id
     * @return
     */
    @GetMapping("sku/{id}")
    public Sku querySkuById(@PathVariable("id")Long id);

}
