package com.guo.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class OrderItemVO {
    private Long skuId;

    private Boolean check = true;

    private String title;

    private String image;


    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;
}
