package com.guo.gulimall.product;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guo.gulimall.product.entity.BrandEntity;
import com.guo.gulimall.product.service.BrandService;

import com.guo.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;

    @Test
    public void test() {
        Long[] catelogPath = categoryService.findCatelogPath(171L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }


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
