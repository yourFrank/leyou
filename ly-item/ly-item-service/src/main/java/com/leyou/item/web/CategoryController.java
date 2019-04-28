package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点查询category list
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam("pid")Long pid){
        return ResponseEntity.ok(categoryService.queryCategoryByPid(pid));

    }
    //品牌编辑
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") long bid){
        List<Category> list = this.categoryService.queryByBrandId(bid);
        if (list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

    //分类删除
    @DeleteMapping("cid/{cid}")
    public ResponseEntity<Void> deleteByCategoryId(@PathVariable("cid") long id){
        categoryService.deleteByCategoryId(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    //分类添加
    @PostMapping
    public ResponseEntity<Void> insertCategoryId(Category category){
        categoryService.insertCategoryId(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
    //分类编辑
    @PutMapping
    public ResponseEntity<Void> updateCategoryId(Category category){
        categoryService.updateCategoryId(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     * 根据id集合查询分类
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
            return ResponseEntity.ok(categoryService.getCategoryNameByIds(ids));

    }


}
