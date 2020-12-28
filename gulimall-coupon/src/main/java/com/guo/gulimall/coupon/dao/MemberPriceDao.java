package com.guo.gulimall.coupon.dao;

import com.guo.gulimall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:02:55
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
