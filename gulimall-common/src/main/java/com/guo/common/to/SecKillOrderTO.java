package com.guo.common.to;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillOrderTO {

    private String orderSn;
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

    private Integer num;

    private Long memberId;
}
