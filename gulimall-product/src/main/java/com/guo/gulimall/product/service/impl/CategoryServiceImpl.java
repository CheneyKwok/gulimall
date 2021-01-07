package com.guo.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;

import com.guo.gulimall.product.dao.CategoryDao;
import com.guo.gulimall.product.entity.CategoryEntity;
import com.guo.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查处所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //2、组成父子的树形结构
        //2-1、查出所有一级分类

        return categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity.getCatId(),categoryEntities)))
                .sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort())))
                .collect(Collectors.toList());
    }

    //递归查处所有菜单的子菜单
    private List<CategoryEntity> getChildren(Long parentId, List<CategoryEntity> allList) {
        return allList.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(parentId))
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity.getCatId(), allList)))
                .sorted(Comparator.comparingInt(c -> (c.getSort() == null ? 0 : c.getSort())))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenusByIds(List<Long> asList) {
        //todo 1、检查当前删除的菜单是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[0]);
    }
    private void findParentPPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPPath(categoryEntity.getParentCid(), paths);
        }
    }
}