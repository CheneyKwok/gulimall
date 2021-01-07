package com.guo.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.product.dto.AttrGroupRelationDto;
import com.guo.gulimall.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:07:48
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatchRelation(List<AttrGroupRelationDto> attrGroupRelationDtos);
}

