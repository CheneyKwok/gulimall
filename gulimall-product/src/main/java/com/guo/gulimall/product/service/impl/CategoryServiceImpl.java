package com.guo.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.gulimall.product.dao.CategoryDao;
import com.guo.gulimall.product.entity.CategoryEntity;
import com.guo.gulimall.product.service.CategoryBrandRelationService;
import com.guo.gulimall.product.service.CategoryService;
import com.guo.gulimall.product.vo.Catelog2Vo;
import lombok.extern.java.Log;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

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
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity.getCatId(), categoryEntities)))
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

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        //级联更新所有关联的数据
        this.updateById(category);
        categoryBrandRelationService.updateCascade(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getFirstLevelCategory() {
        return list(new QueryWrapper<CategoryEntity>().lambda().eq(CategoryEntity::getParentCid, 0));
    }


    // redisson分布式锁
    public Map<String, List<Catelog2Vo>> getCateLogJsonWithRedissonLock() {
        RLock lock = redissonClient.getLock("cateLogJson-lock");
        // 阻塞式等待
        lock.lock();
        Map<String, List<Catelog2Vo>> cateLogJsonFromDb;
        try {
            // 加锁成功，执行业务
            String cache = redisTemplate.opsForValue().get("cateLogJson");
            if (!StringUtils.isEmpty(cache)) {
                cateLogJsonFromDb = JSON.parseObject(cache, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
            } else {
                cateLogJsonFromDb = getCateLogJsonFromDb();
                redisTemplate.opsForValue().set("cateLogJson", JSON.toJSONString(cateLogJsonFromDb));
            }
        } finally {
            lock.unlock();
        }
        return cateLogJsonFromDb;

    }

    // redis分布式锁
    public Map<String, List<Catelog2Vo>> getCateLogJsonWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        // 1、占分布式锁，去redis占
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("Lock", uuid, 300, TimeUnit.SECONDS);
        if (lock != null && lock) {

            Map<String, List<Catelog2Vo>> cateLogJsonFromDb;
            try {
                // 加锁成功，执行业务
                String cache = redisTemplate.opsForValue().get("cateLogJson");
                if (!StringUtils.isEmpty(cache)) {
                    cateLogJsonFromDb = JSON.parseObject(cache, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                    });
                } else {
                    cateLogJsonFromDb = getCateLogJsonFromDb();
                }
                redisTemplate.opsForValue().set("cateLogJson", JSON.toJSONString(cateLogJsonFromDb));
            } finally {
                // 释放锁 保证原子性
                String script = "if redis.call('get',KEYS[1]) == ARGV[1]" +
                        "then return redis.call('del',KEYS[1])" +
                        "else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList("Lock"), uuid);

            }
            return cateLogJsonFromDb;
        } else {
            // 自旋
            return getCateLogJsonWithRedisLock();
        }

    }

    // 本地锁
    public Map<String, List<Catelog2Vo>> getCateLogJsonWithLocalLock() {
        synchronized (this){
            String cache = redisTemplate.opsForValue().get("cateLogJson");
            if (!StringUtils.isEmpty(cache)) {
                return JSON.parseObject(cache, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
            }
            Map<String, List<Catelog2Vo>> cateLogJsonFromDb = getCateLogJsonFromDb();
            redisTemplate.opsForValue().set("cateLogJson", JSON.toJSONString(cateLogJsonFromDb));
            return cateLogJsonFromDb;
        }
    }

    /********************缓存需要注意的问题***********************
     * 1、空结果缓存，解决缓存穿透
     * 2、设置过期时间（加随机值）：解决缓存雪崩
     * 3、* 加锁，解决缓存击穿 *
     */

    @Override
    public Map<String, List<Catelog2Vo>> getCateLogJson() {

        String cateLogJson = redisTemplate.opsForValue().get("cateLogJson");
        if (StringUtils.isEmpty(cateLogJson)) {
//            return getCateLogJsonWithLocalLock();

            return getCateLogJsonWithRedissonLock();

        }
        return JSON.parseObject(cateLogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});

    }

    public Map<String, List<Catelog2Vo>> getCateLogJsonFromDb() {
        log.info("查询了数据库....");
        // 查出所有一级分类
        List<CategoryEntity> firstLevelCategory = getFirstLevelCategory();
        // 封装数据
        return firstLevelCategory.stream()
                .collect(Collectors.toMap(
                        categoryEntity -> categoryEntity.getCatId().toString(),
                        categoryEntity -> {
                            List<CategoryEntity> categoryEntities = list(
                                    new QueryWrapper<CategoryEntity>()
                                            .lambda()
                                            .eq(CategoryEntity::getParentCid, categoryEntity.getCatId()));

                            return categoryEntities.stream()
                                    .map(c -> {
                                        Catelog2Vo catelog2Vo = new Catelog2Vo(c.getCatId().toString(), null, c.getCatId().toString(), c.getName());
                                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = list(
                                                new QueryWrapper<CategoryEntity>()
                                                        .lambda()
                                                        .eq(CategoryEntity::getParentCid, c.getCatId()))
                                                .stream()
                                                .map(item -> new Catelog2Vo.Catelog3Vo(c.getCatId().toString(), item.getCatId().toString(), item.getName()))
                                                .collect(Collectors.toList());

                                        catelog2Vo.setCatalog3List(catelog3Vos);
                                        return catelog2Vo;
                                    })
                                    .collect(Collectors.toList());
                        }));
    }

    private void findParentPPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPPath(categoryEntity.getParentCid(), paths);
        }
    }
}