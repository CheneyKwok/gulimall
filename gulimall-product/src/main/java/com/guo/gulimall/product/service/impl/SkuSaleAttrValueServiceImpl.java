package com.guo.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.gulimall.product.dao.SkuSaleAttrValueDao;
import com.guo.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.guo.gulimall.product.service.SkuSaleAttrValueService;
import com.guo.gulimall.product.vo.SkuItemSaleAttrVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVO> getSaleAttrsBySpuId(Long spuId) {
        return baseMapper.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValues(Long skuId) {

        return baseMapper.getSkuSaleAttrValues(skuId);
    }

}