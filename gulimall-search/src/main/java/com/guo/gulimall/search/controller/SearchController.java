package com.guo.gulimall.search.controller;

import com.guo.gulimall.search.service.MallSearchService;
import com.guo.gulimall.search.vo.SearchParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam) {
        Object o = mallSearchService.search(searchParam);
        return "list";
    }
}
