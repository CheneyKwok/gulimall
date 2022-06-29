package com.guo.gulimall.search.service.impl;

import com.guo.gulimall.search.constant.EsConstant;
import com.guo.gulimall.search.service.MallSearchService;
import com.guo.gulimall.search.vo.SearchParam;
import com.guo.gulimall.search.vo.SearchResult;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.guo.gulimall.search.config.ElasticsearchConfig.COMMON_OPTIONS;

@Service
@RequiredArgsConstructor
public class MallSearchServiceImpl implements MallSearchService {

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam searchParam) {

        SearchRequest searchRequest = buildSearchRequest();
        SearchResult searchResult = null;
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, COMMON_OPTIONS);
            searchResult = buildSearchResult(searchResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searchResult;
    }

    /**
     * 构建检索请求
     * 模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）、排序、分页、高亮、聚合分析
     */
    private SearchRequest buildSearchRequest() {

        // 构建 DSL 语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 1.模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）


        // 2. 排序、分页、高亮


        // 3. 聚合分析

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);

        return searchRequest;
    }

    /**
     *
     * @param searchResponse
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse searchResponse) {
        return null;
    }
}
