package com.guo.gulimall.search.service;

import com.guo.gulimall.search.vo.SearchParam;
import com.guo.gulimall.search.vo.SearchResult;


public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
