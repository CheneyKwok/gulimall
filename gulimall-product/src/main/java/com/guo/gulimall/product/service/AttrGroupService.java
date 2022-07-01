package com.guo.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.product.entity.AttrGroupEntity;
import com.guo.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.guo.gulimall.product.vo.SpuItemAttrGroupVO;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:07:48
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * 根据分类id查出所有的分组及组里的属性
     */
    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrByCatelogId(Long catelogId);

    /**
     * 查出当前 spu 对应的所有属性的分组信息以及当前分组下的所有属性对应的值
     */
    List<SpuItemAttrGroupVO> getAttrGroupWithAttrBySpuId(Long spuId, Long catalogId);
}

