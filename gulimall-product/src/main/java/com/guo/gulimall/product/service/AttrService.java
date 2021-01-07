package com.guo.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.product.dto.AttrGroupRelationDto;
import com.guo.gulimall.product.entity.AttrEntity;
import com.guo.gulimall.product.dto.AttrDto;
import com.guo.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:07:48
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrDto attr);

    PageUtils queryBaseListPage(Map<String, Object> params, String attrType, Long catelogId);

    AttrVo getAttrInfo(Long attrId);

    void updateAttr(AttrDto attr);

    /**
     * 根据分组id查找关联的所有基本属性
     */
    List<AttrEntity> getAttrRelation(Long attrGroupId);

    void deleteRelation(AttrGroupRelationDto[] attrGroupRelationDtos);

    /**
     * 获取当前分组没有关联的所有属性
     */
    PageUtils getNoAttrRelation(Map<String, Object> params, Long attrGroupId);
}

