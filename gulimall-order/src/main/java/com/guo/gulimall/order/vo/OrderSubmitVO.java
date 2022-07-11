package com.guo.gulimall.order.vo;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVO {

    private Long addressId;

    private Integer payType;

    private String orderToken;

    /**
     * 应付价格，验价
     */
    private BigDecimal actualPay;
}
