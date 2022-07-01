package com.guo.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SpuItemAttrGroupVO {
    private String groupName;

    private List<SpuBaseAttrVO> attrs;

}
