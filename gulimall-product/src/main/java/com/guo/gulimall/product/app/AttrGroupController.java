package com.guo.gulimall.product.app;

import com.guo.common.utils.PageUtils;
import com.guo.common.utils.R;
import com.guo.gulimall.product.dto.AttrGroupRelationDto;
import com.guo.gulimall.product.entity.AttrEntity;
import com.guo.gulimall.product.entity.AttrGroupEntity;
import com.guo.gulimall.product.service.AttrAttrgroupRelationService;
import com.guo.gulimall.product.service.AttrGroupService;
import com.guo.gulimall.product.service.AttrService;
import com.guo.gulimall.product.service.CategoryService;
import com.guo.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-25 23:47:49
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;


    @RequestMapping("/attr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R addRelation(@RequestBody List<AttrGroupRelationDto> attrGroupRelationDtos){
        attrAttrgroupRelationService.saveBatchRelation(attrGroupRelationDtos);

        return R.ok();
    }


    @GetMapping("/{attrGroupId}/noattr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R attrRelation(@RequestParam Map<String, Object> params,
                          @PathVariable("attrGroupId") Long attrGroupId){
        PageUtils page = attrService.getNoAttrRelation(params, attrGroupId);

        return R.ok().put("page", page);
    }

    @GetMapping("/{attrGroupId}/attr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> list = attrService.getAttrRelation(attrGroupId);

        return R.ok().put("data", list);
    }

    @GetMapping("/{catelogId}/withattr")
    //@RequiresPermissions("product:attrgroup:list")
    public R getAttrGroupWithAttr(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> list = attrGroupService.getAttrGroupWithAttrByCatelogId(catelogId);

        return R.ok().put("data", list);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
   //@RequiresPermissions("product:attrgroup:list")
    public R listById(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);


        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] paths = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(paths);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
   //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    @RequestMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R deleteRelation(@RequestBody AttrGroupRelationDto[] attrGroupRelationDtos){
        attrService.deleteRelation(attrGroupRelationDtos);

        return R.ok();
    }


}
