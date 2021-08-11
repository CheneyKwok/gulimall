package com.guo.gulimall.search.controller;

import com.guo.common.excepiton.BizCodeEnum;
import com.guo.common.to.es.SKuEsModule;
import com.guo.common.utils.R;
import com.guo.gulimall.search.service.ProductSaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/save")
@RequiredArgsConstructor
public class ElasticSaveController {

    private final ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SKuEsModule> sKuEsModules) {
        boolean res;
        try {
            res = productSaveService.productStatusUp(sKuEsModules);
        } catch (IOException e) {
            log.error("ElasticSaveController商品上架错误: {}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION);
        }
        if (res)
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION);
        else
            return R.ok();
    }

}
