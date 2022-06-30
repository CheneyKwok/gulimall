package com.guo.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.guo.common.to.es.SKuEsModule;
import com.guo.gulimall.search.constant.EsConstant;
import com.guo.gulimall.search.service.MallSearchService;
import com.guo.gulimall.search.vo.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.guo.gulimall.search.config.ElasticsearchConfig.COMMON_OPTIONS;

@Service
@RequiredArgsConstructor
public class MallSearchServiceImpl implements MallSearchService {

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam searchParam) {

        SearchRequest searchRequest = buildSearchRequest(searchParam);
        SearchResult searchResult = null;
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, COMMON_OPTIONS);
            searchResult = buildSearchResult(searchParam, searchResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return searchResult;
    }

    /**
     * 构建检索请求
     * 模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）、排序、分页、高亮、聚合分析
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        // 构建 DSL 语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 1.模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存）

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 模糊匹配
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 分类过滤
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 库存过滤
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 品牌过滤
        if (!CollectionUtils.isEmpty(param.getBrandIds())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandIds()));
        }
        // 价格区间过滤
        if (StringUtils.isNotEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] arr = param.getSkuPrice().split("_");
            if (arr.length == 2) {
                rangeQuery.gte(arr[0]).lte(arr[1]);
            } else if (param.getSkuPrice().startsWith("_")) {
                rangeQuery.lte(arr[0]);
            } else if (param.getSkuPrice().endsWith("_")) {
                rangeQuery.gte(arr[0]);
            }
            boolQuery.filter(rangeQuery);
        }
        // 属性过滤
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        searchSourceBuilder.query(boolQuery);

        // 2. 排序、分页、高亮

        // 排序
        if (StringUtils.isNotEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            searchSourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }

        // 分页
        int from = (param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        // 高亮
        if (StringUtils.isNotEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 3. 聚合分析

        // 品牌聚合
        TermsAggregationBuilder brand_terms = AggregationBuilders.terms("brand_terms");
        brand_terms.field("brandId").size(50);
        brand_terms.subAggregation(AggregationBuilders.terms("brandName_terms").field("brandName").size(1));
        brand_terms.subAggregation(AggregationBuilders.terms("brandImg_terms").field("brandImg").size(1));

        // 分类聚合
        TermsAggregationBuilder catalog_terms = AggregationBuilders.terms("catalog_terms");
        catalog_terms.field("catalogId").size(20);
        catalog_terms.subAggregation(AggregationBuilders.terms("catalogName_terms").field("catalogName").size(1));

        // 属性聚合
        NestedAggregationBuilder attrs_agg = AggregationBuilders.nested("attrs_agg", "attrs");
        TermsAggregationBuilder attrId_terms = AggregationBuilders.terms("attrId_terms");
        attrId_terms.field("attrs.attrId").size(20);
        attrId_terms.subAggregation(AggregationBuilders.terms("attrName_terms").field("attrs.attrName").size(1));
        attrId_terms.subAggregation(AggregationBuilders.terms("attrValue_terms").field("attrs.attrValue").size(1));
        attrs_agg.subAggregation(attrId_terms);

        searchSourceBuilder.aggregation(brand_terms);
        searchSourceBuilder.aggregation(catalog_terms);
        searchSourceBuilder.aggregation(attrs_agg);

        System.out.println("searchSourceBuilder.toString() = " + searchSourceBuilder.toString());

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }

    /**
     * 构建结果数据
     */
    private SearchResult buildSearchResult(SearchParam searchParam, SearchResponse searchResponse) {

        SearchResult searchResult = new SearchResult();
        // 获取命中记录
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        // 1. 返回所有查询到的商品
        List<SKuEsModule> products = new ArrayList<>();
        if (hits != null && hits.length > 0) {
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                SKuEsModule sKuEsModule = JSON.parseObject(sourceAsString, SKuEsModule.class);
                // 获取高亮字段
                HighlightField skuTitleHF = hit.getHighlightFields().get("skuTitle");
                if (skuTitleHF != null) {
                    sKuEsModule.setSkuTitle(skuTitleHF.getFragments()[0].toString());
                }
                products.add(sKuEsModule);
            }
        }
        searchResult.setProducts(products);
        // 获取聚合信息
        Aggregations aggregations = searchResponse.getAggregations();
        // 2. 当前所有商品涉及到的所有品牌信息
        List<BrandVO> brandVOList = new ArrayList<>();
        ParsedLongTerms brand_terms = aggregations.get("brand_terms");
        for (Terms.Bucket bucket : brand_terms.getBuckets()) {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(Long.parseLong(bucket.getKeyAsString()));

            ParsedStringTerms brandName_terms = bucket.getAggregations().get("brandName_terms");
            brandVO.setBrandName(brandName_terms.getBuckets().get(0).getKeyAsString());

            ParsedStringTerms brandImg_terms = bucket.getAggregations().get("brandImg_terms");
            brandVO.setBrandImg(brandImg_terms.getBuckets().get(0).getKeyAsString());

            brandVOList.add(brandVO);
        }
        searchResult.setBrandVOList(brandVOList);
        // 3. 当前所有商品涉及到的所有分类信息
        List<CateLogVO> cateLogVOList = new ArrayList<>();
        ParsedLongTerms catalog_terms = aggregations.get("catalog_terms");
        for (Terms.Bucket bucket : catalog_terms.getBuckets()) {
            CateLogVO cateLogVO = new CateLogVO();
            cateLogVO.setCateLogId(Long.parseLong(bucket.getKeyAsString()));

            ParsedStringTerms catalogName_terms = bucket.getAggregations().get("catalogName_terms");
            cateLogVO.setCateLogName(catalogName_terms.getBuckets().get(0).getKeyAsString());

            cateLogVOList.add(cateLogVO);
        }
        searchResult.setCateLogVOList(cateLogVOList);
        // 4. 当前所有商品涉及到的所有属性信息
        List<AttrVO> attrVOList = new ArrayList<>();
        ParsedNested attrs_agg = aggregations.get("attrs_agg");

        ParsedLongTerms attrId_terms = attrs_agg.getAggregations().get("attrId_terms");
        for (Terms.Bucket bucket : attrId_terms.getBuckets()) {
            AttrVO attrVO = new AttrVO();
            attrVO.setAttrId(Long.valueOf(bucket.getKeyAsString()));

            ParsedStringTerms attrName_terms = bucket.getAggregations().get("attrName_terms");
            attrVO.setAttrName(attrName_terms.getBuckets().get(0).getKeyAsString());

            ParsedStringTerms attrValue_terms = bucket.getAggregations().get("attrValue_terms");
            List<String> attrValueList = attrValue_terms.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVO.setAttrValue(attrValueList);

            attrVOList.add(attrVO);
        }
        searchResult.setAttrVOList(attrVOList);

        // 5. 分页信息-页码
        searchResult.setPageNum(searchParam.getPageNum());
        // 6. 分页信息-总记录数
        long total = searchHits.getTotalHits().value;
        searchResult.setTotal(total);
        // 7. 分页信息-总页码
        int totalPages = (int) Math.ceil(total / (double)EsConstant.PRODUCT_PAGE_SIZE);
        searchResult.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i < totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        return searchResult;
    }
}
