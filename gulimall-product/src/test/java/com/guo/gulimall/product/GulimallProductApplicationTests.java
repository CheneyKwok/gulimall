package com.guo.gulimall.product;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guo.gulimall.product.dao.AttrGroupDao;
import com.guo.gulimall.product.dao.SkuSaleAttrValueDao;
import com.guo.gulimall.product.entity.BrandEntity;
import com.guo.gulimall.product.service.BrandService;
import com.guo.gulimall.product.service.CategoryService;
import com.guo.gulimall.product.vo.SkuItemSaleAttrVO;
import com.guo.gulimall.product.vo.SpuItemAttrGroupVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
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
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;


    @Test
    public void attrGroupDaoTest() {
        List<SpuItemAttrGroupVO> list = attrGroupDao.getAttrGroupWithAttrBySpuId(1L, 225L);
        System.out.println(list);
    }

    @Test
    public void skuSaleAttrValueDaoTest() {
        List<SkuItemSaleAttrVO> list = skuSaleAttrValueDao.getSaleAttrsBySpuId(1L);
        System.out.println(list);
    }

    @Test
    public void test() {
        Long[] catelogPath = categoryService.findCatelogPath(171L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    public void redissonClientTest() {
        System.out.println(redissonClient);
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
