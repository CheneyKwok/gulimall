package com.guo.gulimall.order.vo;


import com.guo.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVO {

    private OrderEntity order;

    private Integer code;
}
