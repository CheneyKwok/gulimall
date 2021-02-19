package com.guo.gulimall.ware;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guo.gulimall.ware.entity.WareSkuEntity;
import com.guo.gulimall.ware.service.WareSkuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallWareApplicationTests {
	@Autowired
	WareSkuService wareSkuService;

	@Test
	void contextLoads() {
		LambdaQueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<WareSkuEntity>()
				.lambda()
				.select(WareSkuEntity::getStockLocked)
				.eq(WareSkuEntity::getSkuId, 1);
		int count = wareSkuService.count(queryWrapper);
	}

}
