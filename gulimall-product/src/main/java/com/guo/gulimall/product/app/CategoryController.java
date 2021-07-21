package com.guo.gulimall.product.app;

import com.guo.common.utils.R;
import com.guo.gulimall.product.entity.CategoryEntity;
import com.guo.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;



/**
 * 商品三级分类
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:47:49
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查处所有分类及子分类，以树形结构组装起来
     */
    @RequestMapping("/list/tree")
   //@RequiresPermissions("product:category:list")
    public R list(){
        List<CategoryEntity> categoryEntityList = categoryService.listWithTree();
        return R.ok().put("data", categoryEntityList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
   //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 批量修改
     */
    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
//		categoryService.updateById(category);
        categoryService.updateCascade(category);
        return R.ok();
    }

    /**
     * 删除
     * RequestBody: 获取请求体，必须是post请求才有
     * springMVC自动将请求体的数据（json）转为对应的对象
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
		categoryService.removeByIds(Arrays.asList(catIds));
		//1、检查当前删除的菜单是否被别的地方引用
        categoryService.removeMenusByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
