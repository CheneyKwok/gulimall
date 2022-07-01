package com.guo.gulimall.product.dao;

import com.guo.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guo.gulimall.product.vo.SkuItemSaleAttrVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:07:48
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVO> getSaleAttrsBySpuId(Long spuId);
}
