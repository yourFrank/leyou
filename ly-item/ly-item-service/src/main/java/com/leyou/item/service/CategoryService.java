package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;


    public List<Category> queryCategoryByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = categoryMapper.select(category);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
    return list;
    }

    public List<Category> queryByBrandId(long bid) {

        return this.categoryMapper.queryByBrandId(bid);
    }


    public void deleteByCategoryId(long id) {

        Category category = categoryMapper.selectByPrimaryKey(id);
        //判断如果删除节点是父类，不能直接删除
        if(category.getIsParent()){
            throw  new  LyException(ExceptionEnum.CATEGORY_DELETE_ERROR);
        }
        categoryMapper.delete(category);

        Category category1=new Category();
        category1.setParentId(category.getParentId());
        List<Category> list = categoryMapper.select(category1);

        //如果不存在子节点，将父节点is_parent设为0;
        if(CollectionUtils.isEmpty(list)){
            Category parentCategory = categoryMapper.selectByPrimaryKey(category.getParentId());
            parentCategory.setIsParent(false);
            categoryMapper.updateByPrimaryKey(parentCategory);
        }


    }

    @Transactional
    public void insertCategoryId(Category category) {

        Category c_parent = categoryMapper.selectByPrimaryKey(category.getParentId());
        if(!c_parent.getIsParent())
         {
            c_parent.setIsParent(true);
            categoryMapper.updateByPrimaryKey(c_parent);
         }
        categoryMapper.insert(category);



    }

    public void updateCategoryId(Category category) {
        categoryMapper.updateByPrimaryKeySelective(category);
    }

    /*
    根据ID获取集合
     */
    public List<Category> getCategoryNameByIds(List<Long> ids){
        List<Category> categories = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(categories)){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return categories;


    }
}


