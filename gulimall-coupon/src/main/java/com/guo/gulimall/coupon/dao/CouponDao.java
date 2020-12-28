package com.guo.gulimall.coupon.dao;

import com.guo.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:02:55
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
