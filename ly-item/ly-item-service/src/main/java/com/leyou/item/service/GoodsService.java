package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.stockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YuTian
 * @date 2019/4/8 8:19
 */
@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private stockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;


    public PageResult<Spu> querySpuByPage(String key, Integer page, Integer rows, Boolean saleable) {
        PageHelper.startPage(page,rows);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria .andLike("title","%"+key+"%");

        }
        if(saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
        //根据更新时间设置排序条件
        example.setOrderByClause("last_update_time DESC");
        List<Spu> spus = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
      //解析分类和品牌
        loadCategoyrAndBrandName(spus);
        //解析分页结果
        PageInfo<Spu> info=new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(),spus);

    }

    //将id改为分类名字和品牌名字
    private void loadCategoyrAndBrandName(List<Spu> spus) {

        for (Spu spu : spus) {
            //处理分类名字
            List<Category> categoryList = categoryService.getCategoryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            List<String> names = categoryList.stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));

            spu.setPname(brandService.getById(spu.getBrandId()));


        }
    }

    public void insertGoods(Spu spu) {
        // 保存spu
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        int count = spuMapper.insert(spu);
        if(count!=1){
            throw  new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        // 保存spu详情
        spu.getSpuDetail().setSpuId(spu.getId());
         count = spuDetailMapper.insert(spu.getSpuDetail());
        if(count!=1){
            throw  new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        // 保存sku和库存信息
        saveSkuAndStock(spu.getSkus(), spu.getId());

        //发送消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
      List<Stock> stocks=new ArrayList<>();
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(spuId);
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
             int count = skuMapper.insert(sku);
            if(count!=1){
                throw  new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);

        }
        //插入stocks List
        stockMapper.insertList(stocks);
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        // 查询sku
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            // 同时查询出库存
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        }
        return skus;
    }

    public SpuDetail querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);

        if (spuDetail==null){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuDetail;
    }

    //商品更新
    @Transactional
    public void updateGoods(Spu spu) {
        // 查询以前sku
        List<Sku> skus = this.querySkuBySpuId(spu.getId());
        // 如果以前存在，则删除
        if(!CollectionUtils.isEmpty(skus)) {
            List<Long> ids = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
            // 删除以前库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);

            // 删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spu.getId());
            this.skuMapper.delete(record);

        }
        // 新增sku和库存
        saveSkuAndStock(spu.getSkus(), spu.getId());

        // 更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);

        // 更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());

        //发送消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    //商品下架
    public void downGood(Long id) {
       Spu spu=new Spu();
       spu.setId(id);
       spu.setSaleable(false);
        spuMapper.updateByPrimaryKeySelective(spu);

    }

    //商品上架
    public void upGood(Long id) {
        Spu spu=new Spu();
        spu.setId(id);
        spu.setSaleable(true);
        spuMapper.updateByPrimaryKeySelective(spu);

    }

    //商品删除
    @Transactional
    public void deleteGoods(Long id) {

        //删除spu
        int i = spuMapper.deleteByPrimaryKey(id);
        if (i!=1){

            throw  new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
        }
        //删除spu_detail
        i = spuDetailMapper.deleteByPrimaryKey(id);
        if (i!=1){
            throw  new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
        }
        //查询该spu下的sku
        Sku sku=new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);

        for (Sku sku1 : skus) {
                //删除stock
            i=stockMapper.deleteByPrimaryKey(sku1.getId());
            //删除sku
            if (i!=1){
                throw  new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
            }
            i = skuMapper.delete(sku1);
            if (i!=1){
                throw  new LyException(ExceptionEnum.GOODS_DELETE_ERROR);
            }
        }

        amqpTemplate.convertAndSend("item.delete",id);
    }

    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu==null){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //为了方便再查询spuDeatail和skus
        List<Sku> skus = querySkuBySpuId(id);
        spu.setSkus(skus);
        SpuDetail spuDetail = querySpuDetailById(id);
        spu.setSpuDetail(spuDetail);
        return spu;


    }

    public Sku querySkuById(Long id) {
        Sku sku = skuMapper.selectByPrimaryKey(id);
        if (sku==null){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return sku;
    }
}
