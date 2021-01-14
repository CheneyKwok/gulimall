package com.guo.gulimall.ware.dto;

import lombok.Data;

@Data
public class PurchaseItemDoneDto {
    //{itemId:1,status:4,reason:""}
    private Long itemId;
    private Integer status;
    private String reason;
}
