package com.guo.gulimall.secskill.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillSkuVO {
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal secKillPrice;
    /**
     * 秒杀总量
     */
    private Integer secKillCount;
    /**
     * 每人限购数量
     */
    private Integer secKillLimit;
    /**
     * 排序
     */
    private Integer secKillSort;

}
