package com.guo.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀活动商品关联
 * 
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:02:55
 */
@Data
@TableName("sms_seckill_sku_relation")
public class SeckillSkuRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 活动id
	 */
	private Long promotionId;
	/**
	 * 活动场次id
	 */
	private Long promotionSessionId;
	/**
	 * 商品id
	 */
	private Long skuId;
	/**
	 * 秒杀价格
	 */
	@TableField("seckill_price")
	private BigDecimal secKillPrice;
	/**
	 * 秒杀总量
	 */
	@TableField("seckill_count")
	private BigDecimal secKillCount;
	/**
	 * 每人限购数量
	 */
	@TableField("seckill_limit")
	private BigDecimal secKillLimit;
	/**
	 * 排序
	 */
	@TableField("seckill_sort")
	private Integer secKillSort;

}
