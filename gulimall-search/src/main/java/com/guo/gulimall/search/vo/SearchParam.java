package com.guo.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 三级分类Id
     */
    private Long cateLogThirdId;

    /**
     * 排序条件
     * sort = saleCount_asc/desc
     * sort = skuPrice_asc/desc
     * sort = hostScore_asc/desc
     */
    private String sort;

    /*
     * 过滤条件
     * hasStock(是否有货)、skuPrice(价格区间)、brandId、cateLogThirdId、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     */

    /**
     * 是否只显示有货
     */
    private Integer hasStock;

    /**
     * 价格区间
     */
    private String skuPrice;

    /**
     * 品牌ID(可多选)
     */
    private List<Long> brandId;

    /**
     * 属性
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum;
}
