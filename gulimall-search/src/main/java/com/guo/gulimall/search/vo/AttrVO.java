package com.guo.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class AttrVO {

    private Long attrId;

    private String attrName;

    private List<String> attrValue;
}
