package com.guo.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.gulimall.coupon.dao.SeckillSessionDao;
import com.guo.gulimall.coupon.entity.SeckillSessionEntity;
import com.guo.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.guo.gulimall.coupon.service.SeckillSessionService;
import com.guo.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {


    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getSecKillSessionsIn3Days() {
        List<SeckillSessionEntity> sessionEntities = lambdaQuery()
                .between(SeckillSessionEntity::getStartTime, getStartTime(), getEndTime())
                .list();

        return sessionEntities
                .stream()
                .peek(session -> {
                    List<SeckillSkuRelationEntity> skuRelationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
                    session.setRelations(skuRelationEntities);
                })
                .collect(Collectors.toList());
    }

    // 当前天数的 00:00:00
    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.atTime(LocalTime.MIN);
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    //当前天数+2 23:59:59
    private String getEndTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.plusDays(2).atTime(LocalTime.MAX);
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}