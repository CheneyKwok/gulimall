package com.guo.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuItemSaleAttrVO {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
