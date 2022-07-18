package com.guo.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.common.utils.R;
import com.guo.gulimall.product.dao.SkuInfoDao;
import com.guo.gulimall.product.entity.SkuImagesEntity;
import com.guo.gulimall.product.entity.SkuInfoEntity;
import com.guo.gulimall.product.entity.SpuInfoDescEntity;
import com.guo.gulimall.product.feign.SecKillFeignService;
import com.guo.gulimall.product.service.*;
import com.guo.gulimall.product.vo.SecKillInfoVO;
import com.guo.gulimall.product.vo.SkuItemSaleAttrVO;
import com.guo.gulimall.product.vo.SkuItemVO;
import com.guo.gulimall.product.vo.SpuItemAttrGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SecKillFeignService secKillFeignService;

    @Autowired
    ThreadPoolExecutor executor;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageBycondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> w.eq("sku_id", key).or().like("sku_name", key));
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal decimal = new BigDecimal(max);
                if (decimal.compareTo(new BigDecimal(0)) > 0) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        return list(new QueryWrapper<SkuInfoEntity>().lambda().eq(SkuInfoEntity::getSpuId, spuId));
    }

    @Override
    public SkuItemVO item(Long skuId) {
        SkuItemVO skuItemVO = new SkuItemVO();
        // 1. sku 基本信息获取
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {

            SkuInfoEntity info = getById(skuId);
            skuItemVO.setInfo(info);
            return info;
        }, executor);

        // 2. sku 的图片信息
        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVO.setImages(imagesEntities);
        }, executor);

        // 3. 获取 sku 的销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(info -> {
            List<SkuItemSaleAttrVO> skuItemSaleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(info.getSpuId());
            skuItemVO.setSaleAttrs(skuItemSaleAttrs);
        }, executor);

        // 4. 获取 spu 的介绍
        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(info -> {
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(info.getSpuId());
            skuItemVO.setDesc(spuInfoDesc);
        }, executor);


        // 5. 获取 spu 的规格参数信息
        CompletableFuture<Void> groupFuture = infoFuture.thenAcceptAsync(info -> {
            List<SpuItemAttrGroupVO> attrGroups = attrGroupService.getAttrGroupWithAttrBySpuId(info.getSpuId(), info.getCatalogId());
            skuItemVO.setGroupAttrs(attrGroups);
        }, executor);

        // 6. 查询当前 sku 是否参与秒杀活动
        CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(() -> {
            R r = secKillFeignService.getSkuSecKillInfo(skuId);
            if (r.getCode() == 0) {
                SecKillInfoVO secKillInfoVO = r.getData(new TypeReference<SecKillInfoVO>() {
                });
                skuItemVO.setSecKillInfoVO(secKillInfoVO);
            }
        }, executor);

        try {
            CompletableFuture.allOf(imagesFuture, saleAttrFuture, descFuture, groupFuture, secKillFuture).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return skuItemVO;
    }

}