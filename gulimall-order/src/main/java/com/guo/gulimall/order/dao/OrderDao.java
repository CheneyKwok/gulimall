package com.guo.gulimall.order.dao;

import com.guo.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:21:34
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
