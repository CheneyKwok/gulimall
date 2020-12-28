package com.guo.gulimall.coupon.dao;

import com.guo.gulimall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:02:55
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
