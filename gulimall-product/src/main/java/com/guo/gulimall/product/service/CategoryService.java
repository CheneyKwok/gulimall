package com.guo.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:07:48
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenusByIds(List<Long> asList);
}

