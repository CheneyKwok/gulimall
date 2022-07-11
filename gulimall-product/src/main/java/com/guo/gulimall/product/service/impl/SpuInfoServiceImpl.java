package com.guo.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.constant.ProductConstant;
import com.guo.common.to.SkuHasStockTo;
import com.guo.common.to.SkuReductionTo;
import com.guo.common.to.SpuBoundTo;
import com.guo.common.to.es.SKuEsModule;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.common.utils.R;
import com.guo.gulimall.product.dao.SpuInfoDao;
import com.guo.gulimall.product.dto.*;
import com.guo.gulimall.product.entity.*;
import com.guo.gulimall.product.feign.CouponFeignService;
import com.guo.gulimall.product.feign.SearchFeignService;
import com.guo.gulimall.product.feign.WareFeignService;
import com.guo.gulimall.product.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService descService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService saleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveDto dto) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(dto, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        //2、保存spu描述图片 pms_spu_info_desc
        List<String> decript = dto.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join("，", decript));
        descService.saveSpuInfoDesc(descEntity);
        //3、保存spu图片集 pms_spu_images
        List<String> images = dto.getImages();
        imagesService.saveImages(spuInfoEntity.getId(), images);
        //4、保存spu规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = dto.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity attrValueEntity = new ProductAttrValueEntity();
            attrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            attrValueEntity.setAttrName(attrEntity.getAttrName());
            attrValueEntity.setAttrValue(attr.getAttrValues());
            attrValueEntity.setQuickShow(attr.getShowDesc());
            attrValueEntity.setSpuId(spuInfoEntity.getId());
            return attrValueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);
        //5、保存spu的积分信息 sms_spu_bounds
        Bounds bounds = dto.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        //6、保存spu对应的sku信息
        //   6.1）sku的基本信息 pms_sku_info
        List<Skus> skus = dto.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {

            skus.forEach(sku -> {
                String defaultImg = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity infoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, infoEntity);
                infoEntity.setBrandId(spuInfoEntity.getBrandId());
                infoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                infoEntity.setSaleCount(0L);
                infoEntity.setSpuId(spuInfoEntity.getId());
                infoEntity.setSkuDefaultImg(defaultImg);
                infoEntity.setSkuName(spuInfoEntity.getSpuName());
                skuInfoService.save(infoEntity);
                Long skuId = infoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = sku.getImages().stream().map(img -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    imagesEntity.setSkuId(skuId);
                    imagesEntity.setImgUrl(img.getImgUrl());
                    imagesEntity.setDefaultImg(img.getDefaultImg());
                    return imagesEntity;
                }).filter(img -> !StringUtils.isEmpty(img.getImgUrl())).collect(Collectors.toList());
                //   6.2）sku的图片信息 pms_sku_images
                //TODO 没有路径的图片无需保存
                skuImagesService.saveBatch(imagesEntities);
                //   6.3）sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity saleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, saleAttrValueEntity);
                    saleAttrValueEntity.setSkuId(skuId);
                    return saleAttrValueEntity;
                }).collect(Collectors.toList());
                saleAttrValueService.saveBatch(saleAttrValueEntities);
                //   6.4）sku的优惠、满减信息 sms_sku_ladder、 sms_sku_full_reduction、 sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w -> w.eq("id", key).or().like("spu_name", key));
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catalogId = (String) params.get("catalogId");
        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            queryWrapper.eq("catalog_id", catalogId);
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {

        // 查出当前spuid对应的sku的信息，品牌的名字
        List<SkuInfoEntity> skuList = skuInfoService.getSkuBySpuId(spuId);

        List<Long> skuIds = skuList.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 查询当前spu所有的可以被检索的属性
        List<ProductAttrValueEntity> baseAttrList = attrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrList.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        HashSet<Long> idSet = new HashSet<>(searchAttrIds); // set集合contains效率高
        List<SKuEsModule.Attrs> attrsList = baseAttrList.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    SKuEsModule.Attrs attrs = new SKuEsModule.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                }).collect(Collectors.toList());

        // 发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {

            R r = wareFeignService.getSkuHasStock(skuIds);
//            List<SkuHasStockTo> data = r.getValue("data", new ArrayList<>());
            stockMap = r.getData(new TypeReference<List<SkuHasStockTo>>(){}).stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常， 原因{}", e);
        }
        // 封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SKuEsModule> upProducts = skuList.stream().map(sku -> {
            SKuEsModule sKuEsModule = new SKuEsModule();
            BeanUtils.copyProperties(sku, sKuEsModule);
            sKuEsModule.setSkuPrice(sku.getPrice());
            sKuEsModule.setSkuImg(sku.getSkuDefaultImg());

            sKuEsModule.setHasStock(finalStockMap == null || finalStockMap.get(sku.getSkuId()));

            // 热度评分 0 可扩展
            sKuEsModule.setHotStore(0L);

            // 查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(sKuEsModule.getBrandId());
            sKuEsModule.setBrandName(brand.getName());
            sKuEsModule.setBrandImg(brand.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(sKuEsModule.getCatalogId());
            sKuEsModule.setCatalogName(categoryEntity.getName());

            sKuEsModule.setAttrs(attrsList);


            return sKuEsModule;
        }).collect(Collectors.toList());

        // 数据发送给es进行保存 gulimall-search
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {

            // 远程调用成功，修改当前spu的状态
            update(new UpdateWrapper<SpuInfoEntity>()
                    .lambda()
                    .set(SpuInfoEntity::getPublishStatus, ProductConstant.StatusEnum.SPU_UP.getCode())
                    .eq(SpuInfoEntity::getId, spuId)
            );

        } else {
            // 远程调用失败
            // TODO 重复调用，接口幂等性
            // Feign的调用过程
            /**
             * 1、构造请求数据，将对象转为json
             *  RequestTemplate template = this.buildTemplateFromArgs.create(argv);
             * 2、发送请求进行执行（执行成功会解码响应数据）
             *  executeAndDecode(template, options)
             *  3、执行请求会有重试机制
             */
        }


    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        return getById(skuInfo.getSpuId());
    }


}