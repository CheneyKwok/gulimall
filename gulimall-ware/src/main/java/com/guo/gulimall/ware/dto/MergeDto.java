package com.guo.gulimall.ware.dto;

import lombok.Data;

import java.util.List;

@Data
public class MergeDto {
    private Long purchaseId;
    private List<Long> items;
}
