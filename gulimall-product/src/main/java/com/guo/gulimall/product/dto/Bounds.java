/**
  * Copyright 2021 json.cn 
  */
package com.guo.gulimall.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Bounds {

    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}