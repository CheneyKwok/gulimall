package com.guo.gulimall.coupon.service.impl;

import com.guo.common.to.MemberPrice;
import com.guo.common.to.SkuReductionTo;
import com.guo.gulimall.coupon.entity.MemberPriceEntity;
import com.guo.gulimall.coupon.entity.SkuLadderEntity;
import com.guo.gulimall.coupon.service.MemberPriceService;
import com.guo.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;

import com.guo.gulimall.coupon.dao.SkuFullReductionDao;
import com.guo.gulimall.coupon.entity.SkuFullReductionEntity;
import com.guo.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService ladderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void saveSkuReduction(SkuReductionTo to) {
        //1、保存sku满减打折、优惠
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(to.getSkuId());
        ladderEntity.setFullCount(to.getFullCount());
        ladderEntity.setDiscount(to.getDiscount());
        ladderEntity.setAddOther(to.getCountStatus());
        if (to.getFullCount() > 0) {
            ladderService.save(ladderEntity);
        }
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(to, fullReductionEntity);
        if (fullReductionEntity.getFullPrice().compareTo(new BigDecimal(0)) > 0) {
            save(fullReductionEntity);
        }
        List<MemberPrice> memberPrices = to.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream().map(memberPrice -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(to.getSkuId());
            priceEntity.setMemberLevelId(memberPrice.getId());
            priceEntity.setMemberLevelName(memberPrice.getName());
            priceEntity.setMemberPrice(memberPrice.getPrice());
            priceEntity.setAddOther(1);
            return priceEntity;
        }).filter(memberPriceEntity -> memberPriceEntity.getMemberPrice().compareTo(new BigDecimal(0)) > 0).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}