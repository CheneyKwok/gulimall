package com.guo.gulimall.product.vo;


import com.guo.gulimall.product.entity.SkuImagesEntity;
import com.guo.gulimall.product.entity.SkuInfoEntity;
import com.guo.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVO {

    private SkuInfoEntity info;

    private List<SkuImagesEntity> images;

    private SpuInfoDescEntity desc;

    private List<SkuItemSaleAttrVO> saleAttrs;

    private List<SpuItemAttrGroupVO> groupAttrs;

    private SecKillInfoVO secKillInfoVO;

    private Boolean hasStock = true;

}
