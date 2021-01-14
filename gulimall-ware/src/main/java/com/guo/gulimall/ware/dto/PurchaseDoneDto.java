package com.guo.gulimall.ware.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneDto {

    @NotNull
    private Long id;//采购单id

    private List<PurchaseItemDoneDto> items;
}
