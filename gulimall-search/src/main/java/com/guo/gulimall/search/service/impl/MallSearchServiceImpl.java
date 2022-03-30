package com.guo.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.guo.common.to.es.SKuEsModule;
import com.guo.gulimall.search.config.ElasticsearchConfig;
import com.guo.gulimall.search.constant.EsConstant;
import com.guo.gulimall.search.service.MallSearchService;
import com.guo.gulimall.search.vo.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam searchParam) {
        // 动态构建出查询需要的DSL语句
        SearchResult result = null;
        SearchRequest request = buildSearchRequest(searchParam);
        try {
            SearchResponse response = client.search(request, ElasticsearchConfig.COMMON_OPTIONS);
            // 封装响应数据
            result = buildSearchResponse(searchParam, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 准备检索请求
     * 模糊匹配、过滤、排序、分页、高亮、聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //模糊匹配、过滤
        // 构建bool-query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        if (ObjectUtils.isNotEmpty(searchParam.getCateLogThirdId())) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", searchParam.getCateLogThirdId()));
        }
        if (CollectionUtils.isNotEmpty(searchParam.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        boolQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        if (StringUtils.isNotEmpty(searchParam.getSkuPrice())) {
            // 1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] arr = searchParam.getSkuPrice().split("_");
            if (arr.length == 2) {
                // 区间
                rangeQuery.gte(arr[0]).lt(arr[1]);
            } else if (arr.length == 1) {
                if (searchParam.getSkuPrice().startsWith("_")) {
                    rangeQuery.lt(arr[0]);
                }
                if (searchParam.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(arr[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        if (CollectionUtils.isNotEmpty(searchParam.getAttrs())) {
            for (String attr : searchParam.getAttrs()) {
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
        builder.query(boolQuery);
        // 排序
        if (StringUtils.isNotEmpty(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] arr = sort.split("_");
            SortOrder order = arr[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(arr[0], order);
        }
        // 分页
        int from = (searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE;
        builder.from(from);
        builder.size(EsConstant.PRODUCT_PAGE_SIZE);
        // 高亮
        if (StringUtils.isNotEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }
        //聚合分析
        //3.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //子聚合 品牌
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName")).size(1);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg")).size(1);
        builder.aggregation(brand_agg);

        //3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        //子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        builder.aggregation(catalog_agg);

        //3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //子聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //子子聚合 2个
        //聚合分析出当前所有attrId对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName")).size(1);
        //聚合分析出当前attrid对应的所有可能的属性值 attrvalue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue")).size(50);
        attr_agg.subAggregation(attr_id_agg);
        builder.aggregation(attr_agg);

        System.out.println(builder.toString());

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
    }

    private SearchResult buildSearchResponse(SearchParam searchParam, SearchResponse response) {
        //要封装的大对象
        SearchResult result = new SearchResult();
        //1 封装返回的所有查询到的商品
        ArrayList<SKuEsModule> esModels = new ArrayList<>();
        SearchHits hits = response.getHits();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SKuEsModule esModel = JSON.parseObject(sourceAsString, SKuEsModule.class);
                //高亮
                if (!org.springframework.util.StringUtils.isEmpty(searchParam.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //2 当前所有商品涉及到的所有属性信息 Aggregation -> ParsedNested
        ArrayList<AttrVO> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        //nested的第一层 聚合 Aggregation -> ParsedLongTerms
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            //要封装的小对象
            AttrVO attrVo = new AttrVO();
            //得到属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            //子聚合 得到属性名 Aggregation -> ParsedStringTerms
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            attrVo.setAttrName(attrName);
            //子聚合 复杂 得到属性值 Aggregation -> ParsedStringTerms
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            //因为这个属性是List
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }
        result.setAttrVOList(attrVos);

        //3 当前所有商品所涉及的品牌信息 Aggregation -> ParsedLongTerms
        ArrayList<BrandVO> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            //要封装的小对象
            BrandVO brandVo = new BrandVO();
            //得到品牌id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //子聚合 得到品牌名 Aggregation -> ParsedStringTerms
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            brandVo.setBrandName(brandName);
            //子聚合 得到品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrandVOList(brandVos);

        //4 当前所有商品所涉及到的所有分类信息 Aggregation -> ParsedLongTerms
        ArrayList<CateLogVO> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            //要封装的小对象
            CateLogVO catalogVo = new CateLogVO();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCateLogId(Long.parseLong(keyAsString));
            //子聚合 得到分类名 Aggregation -> ParsedStringTerms
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            catalogVo.setCateLogName(catalog_name);

            catalogVos.add(catalogVo);
        }
        result.setCateLogVOList(catalogVos);
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        int totalPages = (int) (total % EsConstant.PRODUCT_PAGE_SIZE == 0 ? total / EsConstant.PRODUCT_PAGE_SIZE : (total / EsConstant.PRODUCT_PAGE_SIZE + 1));
        result.setTotalPages(totalPages);
        return result;
    }
}
