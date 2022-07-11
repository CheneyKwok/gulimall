package com.guo.gulimall.order.vo;

import lombok.Data;

import java.util.List;


@Data
public class WareSkuLockVO {

    private String orderSn;

    /**
     * 需要锁住的商品
     */
    private List<OrderItemVO> locks;
}
