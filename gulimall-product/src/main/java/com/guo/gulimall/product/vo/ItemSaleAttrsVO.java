package com.guo.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class ItemSaleAttrsVO {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;
}
