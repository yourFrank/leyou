package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YuTian
 * @date 2019/4/17 12:01
 */
@Service
@Slf4j
public class SearchService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient  categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specClient;
    @Autowired
    private GoodsRepository repository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public Goods buildGoods(Spu spu){

        Long spuId = spu.getId();
        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if(CollectionUtils.isEmpty(categories)){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw  new LyException(ExceptionEnum.BRANDS_NOT_FOUND);
        }
        //拼接搜索字段
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        String all=spu.getTitle()+StringUtils.join(names," ")+brand.getName();

        //查询sku信息
        List<Sku> skusList = goodsClient.querySkuBySpuId(spuId);
        //        //这里我们用不到所有的sku信息，又不想重新建一个对象。因此将他封装到一个Map里，转成json后格式一样
        List<Map<String,Object>> skus=new ArrayList<>();
        //skuId的价格集合
        Set<Long> priceList=new HashSet<>();
        for (Sku sku : skusList) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("images",StringUtils.substringBefore(sku.getImages(),","));
            map.put("price",sku.getPrice());
            //添加skuId的价格集合
            priceList.add(sku.getPrice());

            skus.add(map);
        }

        //规格参数（存在两个表，key在spu_param,value在spu_detail）
        Map<String,Object> specMap=new HashMap<>();
        List<SpecParam> specParams = specClient.querySpecParamByGroupId(null, spu.getCid3(), true);
        if(CollectionUtils.isEmpty(specParams)){
            throw  new LyException(ExceptionEnum.SPECPARAM_NOT_FOUND);
        }

        //规格参数的值
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
        if (spuDetail == null) {
            throw  new LyException(ExceptionEnum.SPECPARAM_NOT_FOUND);
        }
        //通用规格参数,因为我们下面要对其kv进行遍历，将其转换成map
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //因为规格参数json的value里第二个是集合，因此用nativeRead进行转换
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        for (SpecParam specParam : specParams) {
            String key=specParam.getName();
            Long id = specParam.getId();
            Object value="";
            if (specParam.getGeneric()){

                 value = genericSpec.get(id);//这里我们直接将其分割成具体的段，方便查询的时候用
                if (specParam.getNumeric()){
                    value= chooseSegment(value.toString(), specParam);
                }
            }else {
                //special value为集合，不用转段
                value=specialSpec.get(id);
            }

            specMap.put(key,value);
        }

        Goods goods=new Goods();
        goods.setId(spuId);
        goods.setBrandId(spu.getBrandId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setAll(all);// 所有需要被搜索的信息，包含标题，分类，甚至品牌
        goods.setCreateTime(spu.getCreateTime());
        goods.setSkus(JsonUtils.toString(skus));// sku信息的json结构
        goods.setPrice(priceList); //SKuId的价格集合
        goods.setSpecs(specMap);// 可搜索的规格参数，key是参数名，值是参数值
        return goods;

    }


    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    public SearchResult searchGoods(SearchRequest searchRequest) {
        Integer page = searchRequest.getPage()-1;
        Integer size = searchRequest.getSize();
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件
        QueryBuilder basicQuery = buildBasicQuery(searchRequest);
        nativeSearchQueryBuilder.withQuery(basicQuery);
        //只查需要字段，过滤条件不查
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"subTitle","skus","id"},null));
        //添加分页条件
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page,size));
        //添加排序条件
        Boolean desc=searchRequest.getDescending();
        String sortBy = searchRequest.getSortBy();
        if(StringUtils.isNotBlank(sortBy)){

        nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc?SortOrder.DESC:SortOrder.ASC));
        }
        //添加聚合
        String categoryName="category";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryName).field("cid3"));
        String brandName="brand";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandName).field("brandId"));

        //查询获取结果
        AggregatedPage<Goods> goodsPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        long total = goodsPage.getTotalElements();
        Integer totalPages = goodsPage.getTotalPages();
        List<Goods> content = goodsPage.getContent();
        int totalPage = ((int)total + size -1) / size;
        //获取聚合结果
        Aggregations aggregations = goodsPage.getAggregations();
        //通过方法将id集合转换为实体类集合返回
        List<Category> categories=getCategoriesByTerm(aggregations.get(categoryName));
        List<Brand> brands=getBrandsByTerms(aggregations.get(brandName));
        //完成规格参数聚合
        List<Map<String,Object> > specs=null;
        if (categories!=null && categories.size()==1){
            specs=buildSpecAgg(categories.get(0).getId(),basicQuery);
        }
        return new SearchResult(total,totalPage,content,categories,brands,specs);


    }

    private QueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        Map<String, String> filter = searchRequest.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value=entry.getValue();
            if (!"cid3".equals(key) && !"brandId".equals(key)){
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,value));
        }
        return boolQueryBuilder;
    }

    //根据分类id聚合规格参数
    private List<Map<String, Object>> buildSpecAgg(Long id, QueryBuilder basicQuery) {
        List<Map<String, Object>> list=new ArrayList<>();
        List<SpecParam> specParams = specClient.querySpecParamByGroupId(null, id, true);
        NativeSearchQueryBuilder queryBuilder=new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        //根据每一个参数进行聚合
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        AggregatedPage<Goods> goods = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        //解析聚合
        Aggregations aggregations = goods.getAggregations();
        //根据参数取聚合结果
        for (SpecParam specParam : specParams) {
            String name=specParam.getName();
            StringTerms aggregation = aggregations.get(name);

            Map<String,Object> map=new HashMap();
            map.put("k",name);
            map.put("options", aggregation.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            list.add(map);
        }

        return list;
    }

    private List<Category> getCategoriesByTerm(LongTerms terms) {
        try {
            List<Long> idList = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(idList);
            return categories;
        }catch (Exception e){
            log.error("[搜索服务]查询分类异常",e);
            return null;
        }


    }

    private List<Brand> getBrandsByTerms(LongTerms terms) {
        try {
            List<Long> idList = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> list = brandClient.queryBrandsByIds(idList);
            return list;
        }catch (Exception e){
            log.error("[搜索服务]查询品牌异常",e);
            return null;
        }


    }

    public void createOrUpdateIndex(Long id) {
        Spu spu = goodsClient.querySpuById(id);
        Goods goods = buildGoods(spu);
        repository.save(goods);

    }

    public void deleteIndex(Long id) {
        repository.deleteById(id);
    }
}
