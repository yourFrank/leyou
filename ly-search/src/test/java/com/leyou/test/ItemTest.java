package com.leyou.test;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author YuTian
 * @date 2019/4/16 8:41
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemRepository itemRepository;
    //使用template创建索引
    @Test
    public void createTest(){
        // 创建索引，会根据Item类的@Document注解信息来创建
        elasticsearchTemplate.createIndex(Item.class);
        // 配置映射，会根据Item类中的id、Field等字段来自动完成映射
        elasticsearchTemplate.putMapping(Item.class);

    }

    /**
     *   使用ItemRepository操作数据
     */
    @Test
    public void index(){

        //创建构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //添加查询条件
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title","小米"));
        //_source过滤
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","title"},null));
        //排序
        nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        //添加分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(0,2));
        Page<Item> search = itemRepository.search(nativeSearchQueryBuilder.build());
        //总记录数
        long size = search.getTotalElements();
        System.out.println("size = " + size);
        //分页页数
        int totalPages = search.getTotalPages();
        System.out.println("totalPages = " + totalPages);
       //当前页的结果
        List<Item> content = search.getContent();
        for (Item item : content) {
            System.out.println("item = " + item);
        }
    }

    @Test
    public void aggs(){
        String aggName="popular_brand";
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(aggName).field("brand"));
        AggregatedPage<Item> items = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Item.class);

        //解析对象
        Aggregations aggregations = items.getAggregations();
        //获取指定名称的聚合
        StringTerms aggregation = aggregations.get(aggName);
        //获取桶
        List<StringTerms.Bucket> buckets = aggregation.getBuckets();
        for (StringTerms.Bucket bucket : buckets) {
            System.out.println("key = " + bucket.getKey());
            System.out.println("bucket.getDocCount() = " + bucket.getDocCount());
        }
    }



}