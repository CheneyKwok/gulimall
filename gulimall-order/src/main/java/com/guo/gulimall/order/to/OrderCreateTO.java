package com.guo.gulimall.order.to;

import com.guo.gulimall.order.entity.OrderEntity;
import com.guo.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
public class OrderCreateTO {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;
}
