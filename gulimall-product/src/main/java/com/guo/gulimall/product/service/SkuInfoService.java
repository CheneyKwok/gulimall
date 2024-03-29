package com.guo.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.product.entity.SkuInfoEntity;
import com.guo.gulimall.product.vo.SkuItemVO;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:07:48
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageBycondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkuBySpuId(Long spuId);

    SkuItemVO item(Long skuId);
}

