package com.guo.gulimall.search.vo;

import com.guo.common.to.es.SKuEsModule;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SKuEsModule> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    private List<Integer> pageNavs;

    /**
     * 所有涉及的品牌
     */
    private List<BrandVO> brandVOList;

    /**
     * 分类
     */
    private List<CateLogVO> cateLogVOList;

    /**
     * 属性
     */
    private List<AttrVO> attrVOList;

    /* 面包屑导航数据 */
    private List<NavVo> navs;

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

}
