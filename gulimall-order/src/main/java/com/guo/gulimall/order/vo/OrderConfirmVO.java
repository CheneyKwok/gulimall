package com.guo.gulimall.order.vo;


import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /**
     * 防重令牌
     */
    String orderToken;

    Map<Long, Boolean> skuStockMap;

    public BigDecimal getTotalPay() {
        if (!CollectionUtils.isEmpty(itemList)) {
            return itemList
                    .stream()
                    .map(e -> e.getPrice().multiply(new BigDecimal(e.getCount())))
                    .reduce(new BigDecimal(0), BigDecimal::add);
        }
        return totalPay;
    }

    public BigDecimal getActualPay() {
        return getTotalPay();
    }

    public int getCount() {
        return itemList.stream().map(OrderItemVO::getCount).reduce(0, Integer::sum);
    }
}
