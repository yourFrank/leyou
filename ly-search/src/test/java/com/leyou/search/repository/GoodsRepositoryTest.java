package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author YuTian
 * @date 2019/4/17 14:34
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void index(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
    }
    @Test
    public void insert(){
        int page=1;
        int rows=100;
        int size=0;
        do {

            PageResult<Spu> spuPageResult = goodsClient.querySpuByPage(null, page, rows, true);
            List<Spu> spus = spuPageResult.getItems();
            //构建成goods
            List<Goods> goodsList = spus.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //存入索引库
            goodsRepository.saveAll(goodsList);
            //翻页
            page++;
            size =spus.size();
        }while (size==100);



    }


}