package com.guo.common.excepiton;

import lombok.Getter;
import lombok.Setter;

public class NoStockException extends RuntimeException{

    @Getter
    @Setter
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id:"+skuId+";库存不足");
    }

    public NoStockException(String message) {
        super(message);
    }
}