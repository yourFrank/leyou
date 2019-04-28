package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/3/31 12:31
 */

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
        @RequestParam(value="key",required = false) String key,
        @RequestParam(value = "page",defaultValue = "1") Integer page ,
        @RequestParam(value = "rows",defaultValue = "5") Integer rows,
        @RequestParam(value = "sortBy",required = false) String sortBy,
        @RequestParam(value = "desc",defaultValue = "false") boolean desc
    ){
        PageResult<Brand> brandPageResult = brandService.queryBrandByPage(key, page, rows, sortBy, desc);
        return ResponseEntity.ok(brandPageResult);

    }

    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam("cids") List<Long> cids){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    //品牌删除
    @DeleteMapping
    public ResponseEntity<Void> deleteBrandById(@RequestParam("id") long id){
        brandService.deleteBrandById(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();


    }
    @PutMapping
    public ResponseEntity<Void> EditBrand(Brand brand,@RequestParam("cids") List<Long> cids){
        brandService.EditBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //品牌删除
    @DeleteMapping("bid/{bid}")
    public ResponseEntity<Void> deleteByBrandId(@PathVariable("bid") long bid){
        brandService.deleteByBrandId(bid);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    //新增商品时选择分类品牌回显
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandsByCid(@PathVariable("cid") long cid){

        return ResponseEntity.ok(brandService.queryBrandsByCid(cid));

    }

    /**
     * 根据id查品牌
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id") Long id){
        return ResponseEntity.ok(brandService.queryBrandById(id));
    }
    @GetMapping("list")
    ResponseEntity<List<Brand>> queryBrandsByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(brandService.queryBrandsByIds(ids));
    }

}
