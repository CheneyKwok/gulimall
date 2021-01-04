package com.guo.gulimall.product;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guo.gulimall.product.entity.BrandEntity;
import com.guo.gulimall.product.service.BrandService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;


    @Test
    void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();
//		brandEntity.setName("华为");
//		brandService.save(brandEntity);

//		brandEntity.setBrandId(1L);
//		brandEntity.setDescript("小米");
//		brandService.updateById(brandEntity);

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach(System.out::println);

    }

}
