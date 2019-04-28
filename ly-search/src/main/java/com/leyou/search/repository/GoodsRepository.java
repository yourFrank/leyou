package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author YuTian
 * @date 2019/4/13 17:56
 */
public interface GoodsRepository  extends ElasticsearchRepository<Goods,Long> {
}

