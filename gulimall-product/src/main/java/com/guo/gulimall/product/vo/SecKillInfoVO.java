package com.guo.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class SecKillInfoVO {

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
    private BigDecimal secKillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal secKillLimit;
    /**
     * 排序
     */
    private Integer secKillSort;

    //当前sku的秒杀开始时间
    private Long startTime;

    //当前sku的秒杀结束时间
    private Long endTime;

    //秒杀随机码
    private String randomCode;
}
