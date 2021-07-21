package com.guo.gulimall.product.app;

import com.guo.common.utils.PageUtils;
import com.guo.common.utils.R;
import com.guo.gulimall.product.dto.AttrDto;
import com.guo.gulimall.product.entity.ProductAttrValueEntity;
import com.guo.gulimall.product.service.AttrService;
import com.guo.gulimall.product.service.ProductAttrValueService;
import com.guo.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品属性
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:47:49
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

//    @GetMapping("/base/list/{catelogId}")
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseList(@RequestParam Map<String, Object> params,@PathVariable("attrType") String attrType, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrService.queryBaseListPage(params, attrType, catelogId);
        return R.ok().put("page", page);
    }

    @RequestMapping("/base/listforspu/{spuId}")
    //@RequiresPermissions("product:attr:info")
    public R baseListForSpu(@PathVariable("spuId") Long spuId){
//		AttrEntity attr = attrService.getById(attrId);

        List<ProductAttrValueEntity> list =  productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", list);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
   //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
//		AttrEntity attr = attrService.getById(attrId);

        AttrVo attr =  attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
   //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrDto attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrDto attr){
//		attrService.updateById(attr);

        attrService.updateAttr(attr);
        return R.ok();
    }

    @RequestMapping("/update/{spuId}")
    //@RequiresPermissions("product:attr:update")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> entities){
//		attrService.updateById(attr);

        productAttrValueService.updateSpuAttr(spuId, entities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
