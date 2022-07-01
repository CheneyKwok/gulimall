package com.guo.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.guo.gulimall.product.vo.SkuItemSaleAttrVO;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:07:48
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVO> getSaleAttrsBySpuId(Long spuId);
}

