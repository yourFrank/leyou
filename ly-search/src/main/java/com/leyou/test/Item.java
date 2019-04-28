package com.leyou.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author YuTian
 * @date 2019/4/16 8:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "item",type = "docs",replicas = 0,shards = 1)
public class Item {
    @Id
    Long id;
    @Field(type =FieldType.text ,analyzer = "ik_max_word")
    String title; //标题
    @Field(type = FieldType.keyword)
    String category;// 分类
    @Field(type = FieldType.keyword)
    String brand; // 品牌
    @Field(type = FieldType.Double)
    Double price; // 价格
    @Field(type = FieldType.keyword,index = false)
    String images; // 图片地址
}
