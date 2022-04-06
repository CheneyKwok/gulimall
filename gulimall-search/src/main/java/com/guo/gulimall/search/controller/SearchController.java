package com.guo.gulimall.search.controller;

import com.guo.gulimall.search.service.MallSearchService;
import com.guo.gulimall.search.vo.SearchParam;
import com.guo.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 根据页面提交的参数去es中查询
     * @param searchParam
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model) {
        SearchResult resp = mallSearchService.search(searchParam);
        model.addAttribute("result", resp);
        return "list";
    }
}
