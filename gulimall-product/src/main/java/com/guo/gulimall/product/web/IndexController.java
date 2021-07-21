package com.guo.gulimall.product.web;

import com.guo.gulimall.product.entity.CategoryEntity;
import com.guo.gulimall.product.service.CategoryService;
import com.guo.gulimall.product.vo.Catelog2Vo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model) {

        List<CategoryEntity> list = categoryService.getFirstLevelCategory();
        model.addAttribute("category", list);
        return "index";
    }

    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        return categoryService.getCatelogJson();
    }

}
