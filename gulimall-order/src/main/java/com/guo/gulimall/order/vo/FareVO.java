package com.guo.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class FareVO {

    private MemberAddressVO address;

    private BigDecimal fare;
}
