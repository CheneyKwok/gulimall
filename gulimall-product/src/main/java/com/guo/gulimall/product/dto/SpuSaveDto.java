/**
  * Copyright 2021 json.cn 
  */
package com.guo.gulimall.product.dto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpuSaveDto {

    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;

}