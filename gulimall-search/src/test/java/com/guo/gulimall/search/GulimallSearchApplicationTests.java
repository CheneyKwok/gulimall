package com.guo.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.guo.gulimall.search.config.ElasticsearchConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {
    @Autowired
    RestHighLevelClient esRestClient;

    @Test
    public void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchQuery("address", "mill"));
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        builder.aggregation(ageAgg);
        AvgAggregationBuilder balanceAgg = AggregationBuilders.avg("balanceAvg").field("balance");
        builder.aggregation(balanceAgg);
        //指定检索条件
        searchRequest.source(builder);
        //执行检索
        SearchResponse searchResponse = esRestClient.search(searchRequest, ElasticsearchConfig.COMMON_OPTIONS);
//        System.out.println("searchResponse = " + searchResponse);
//        Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit hit : searchHits) {
            String string = hit.getSourceAsString();
            Account account = JSON.parseObject(string, Account.class);
            System.out.println("account = " + account);
        }
        Aggregations aggregations = searchResponse.getAggregations();
        for (Aggregation aggregation : aggregations) {
            String name = aggregation.getName();
            System.out.println("当亲聚合 = " + name);
            Aggregation aggregation1 = aggregations.get(name);
            String type = aggregation1.getType();
            System.out.println("type = " + type);
            if (type.equals("lterms")) {
                Terms terms = (Terms) aggregation1;
                for (Terms.Bucket bucket : terms.getBuckets()) {
                    String keyAsString = bucket.getKeyAsString();
                    System.out.println("年龄 = " + keyAsString);
                }
            }
            if ("avg".equalsIgnoreCase(type)) {
                Avg avg = (Avg) aggregation1;
                double value = avg.getValue();
                System.out.println("平均薪资 = " + value);
            }

        }

    }

    // 测试存储数据到es
    @Test
    void contextLoads() throws IOException {
        IndexRequest request = new IndexRequest("users");
        request.id("1");
//      request.source("userName", "张三", "age", 18);  方式一
        //方式二
        User user = new User();
        user.setUserName("张三");
        user.setAge(18);
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString, XContentType.JSON);
        IndexResponse response = esRestClient.index(request, ElasticsearchConfig.COMMON_OPTIONS);
        System.out.println("response = " + response);
    }

    @Data
    class User{
        private String userName;
        private int age;
    }


    @NoArgsConstructor
    @Data
    public static class Account {
        private Integer accountNumber;
        private Integer balance;
        private String firstname;
        private String lastname;
        private Integer age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
}
