package com.guo.gulimall.product.service.impl;

import com.guo.gulimall.product.entity.AttrEntity;
import com.guo.gulimall.product.service.AttrService;
import com.guo.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.guo.gulimall.product.vo.SpuItemAttrGroupVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;

import com.guo.gulimall.product.dao.AttrGroupDao;
import com.guo.gulimall.product.entity.AttrGroupEntity;
import com.guo.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId != 0L) {
            wrapper.and(obj -> obj.eq("catelog_id", catelogId));
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        //查询分组信息
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //查询所有属性

        return attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo withAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, withAttrsVo);
            List<AttrEntity> attrs = attrService.getAttrRelation(item.getAttrGroupId());
            withAttrsVo.setAttrs(attrs);
            return withAttrsVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpuItemAttrGroupVO> getAttrGroupWithAttrBySpuId(Long spuId, Long catalogId) {
        return getBaseMapper().getAttrGroupWithAttrBySpuId(spuId, catalogId);
    }

}