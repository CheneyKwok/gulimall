package com.guo.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.guo.common.to.es.SKuEsModule;
import com.guo.gulimall.search.config.ElasticsearchConfig;
import com.guo.gulimall.search.constant.EsConstant;
import com.guo.gulimall.search.service.ProductSaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSaveServiceImpl implements ProductSaveService {

    private final RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SKuEsModule> sKuEsModules) throws IOException {

        // 保存到es
        // 给es中建立索引。product，建立好映射关系

        // 给es中保存这些数据 BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SKuEsModule module : sKuEsModules) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(module.getSkuId().toString());

            indexRequest.source(JSON.toJSONString(module), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticsearchConfig.COMMON_OPTIONS);
        // TODO 如果批量错误需处理
        boolean hasFailures = bulk.hasFailures();
        if (hasFailures) {
            List<String> list = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
            log.error("商品上架错误：{}", list);
        }

        return hasFailures;

    }
}
