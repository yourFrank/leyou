package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/3/31 12:31
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, boolean desc) {
        //开启分页助手
        PageHelper.startPage(page,rows);
        Example example = new Example(Brand.class);

        //添加过滤条件
        if(StringUtils.isNotBlank(key)){
        example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());

        }
        //添加排序条件
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause=sortBy+( desc ? " DESC":" ASC");
            example.setOrderByClause(orderByClause);
        }
            //查询
        List<Brand> list = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw  new LyException(ExceptionEnum.BRANDS_NOT_FOUND);
        }
        //解析分页助手
        PageInfo<Brand> pageInfo=new PageInfo(list);

        return new PageResult<>(pageInfo.getTotal(),list);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        if(brand!=null) {
            brand.setId(null);
            int i = brandMapper.insert(brand);
            if(i!=1){
                throw  new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }

        //插入category_brand表
        for (Long cid : cids) {
            //插入完会返回新生成的pid
            int i = brandMapper.inserCategoryBrand(cid, brand.getId());
            if(i!=1){
                throw  new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }

    }

    public void deleteBrandById(Long id) {
        brandMapper.deleteByPrimaryKey(id);
    }

    public void EditBrand(Brand brand, List<Long> cids) {

        brandMapper.updateByPrimaryKey(brand);

        for (Long cid : cids) {
            //插入完会返回新生成的pid
             brandMapper.updateCategoryBrand(cid, brand.getId());

        }

    }

    public void deleteByBrandId(long bid) {
        brandMapper.deleteByPrimaryKey(bid);
    }

    public String getById(long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);

        if(brand==null){
            throw  new LyException(ExceptionEnum.BRANDS_NOT_FOUND);
        }
       return  brand.getName();
    }
    public Brand queryBrandById(long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);

        if(brand==null){
            throw  new LyException(ExceptionEnum.BRANDS_NOT_FOUND);
        }
       return  brand;
    }



    public List<Brand> queryBrandsByCid(long cid) {
        List<Brand> brands = brandMapper.queryByCategoryId(cid);
        if (CollectionUtils.isEmpty(brands)){
            throw  new LyException(ExceptionEnum.BRANDS_NOT_FOUND);
        }
        return brands;
    }

    public List<Brand> queryBrandsByIds(List<Long> ids) {
        return brandMapper.selectByIdList(ids);
    }
}
