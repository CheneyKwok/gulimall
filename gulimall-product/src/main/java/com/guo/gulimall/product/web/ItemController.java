package com.guo.gulimall.product.web;

import com.guo.gulimall.product.service.SkuInfoService;
import com.guo.gulimall.product.vo.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId) {

        SkuItemVO skuItemVO = skuInfoService.item(skuId);
        return "item";
    }
}
