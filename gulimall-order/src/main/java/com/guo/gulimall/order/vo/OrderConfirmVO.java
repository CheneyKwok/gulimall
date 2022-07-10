package com.guo.gulimall.order.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVO {

    /**
     * 收货地址
     */
    List<MemberAddressVO> addressList;

    /**
     * 所有选中的购物项
     */
    List<OrderItemVO> itemList;

    /**
     * 优惠券
     */
    Integer integration;

    /**
     * 订单总额
     */
    BigDecimal totalPay;

    /**
     * 应付
     */
    BigDecimal actualPay;

}
