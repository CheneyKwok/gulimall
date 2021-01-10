package com.guo.gulimall.product.vo;

import com.guo.gulimall.product.dto.AttrDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttrVo extends AttrDto{
    /**
     * 所属分类名字
     */
    private String catelogName;
    /**
     * 所属分组名字
     */
    private String groupName;

    private Long[] catelogPath;

    private Integer valueType;
}