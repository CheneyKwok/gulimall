package com.guo.gulimall.ware.controller;

import com.guo.common.excepiton.BizCodeEnum;
import com.guo.common.to.SkuHasStockTo;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.R;
import com.guo.gulimall.ware.entity.WareSkuEntity;
import com.guo.gulimall.ware.service.WareSkuService;
import com.guo.gulimall.ware.vo.WareSkuLockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 商品库存
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:26:17
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVO wareSkuLockVO) {
        try {
            wareSkuService.orderLockStock(wareSkuLockVO);
            return R.ok();
        } catch (Exception e) {
            BizCodeEnum codeEnum = BizCodeEnum.NO_STOCK_EXCEPTION;
            return R.error(codeEnum.getCode(), codeEnum.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
   //@RequiresPermissions("ware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
   //@RequiresPermissions("ware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    // 查询是否有库存
    @PostMapping("/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockTo> list = wareSkuService.getSkuHasStock(skuIds);

        return R.ok().put("data", list);
    }

}
