package com.guo.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guo.common.excepiton.NoStockException;
import com.guo.common.to.SkuHasStockTo;
import com.guo.common.utils.PageUtils;
import com.guo.common.utils.Query;
import com.guo.common.utils.R;
import com.guo.gulimall.ware.dao.WareSkuDao;
import com.guo.gulimall.ware.entity.WareSkuEntity;
import com.guo.gulimall.ware.feign.ProductFeignService;
import com.guo.gulimall.ware.service.WareSkuService;
import com.guo.gulimall.ware.vo.OrderItemVO;
import com.guo.gulimall.ware.vo.WareSkuLockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(id -> {
            SkuHasStockTo stockDto = new SkuHasStockTo();
            LambdaQueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<WareSkuEntity>()
                    .lambda()
                    .select(WareSkuEntity::getStockLocked)
                    .eq(WareSkuEntity::getSkuId, id);
            int count = count(queryWrapper);
            stockDto.setSkuId(id);
            stockDto.setHasStock(count > 0);
            return stockDto;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = NoStockException.class) // 默认运行时异常都会回滚
    @Override
    public boolean orderLockStock(WareSkuLockVO wareSkuLockVO) {

        List<OrderItemVO> locks = wareSkuLockVO.getLocks();
        List<SkuWareHasStock> skuWareHasStocks = locks.stream()
                .map(e -> {
                    SkuWareHasStock stock = new SkuWareHasStock();
                    Long skuId = e.getSkuId();
                    stock.skuId = skuId;
                    List<Long> wareIds = baseMapper.listWareWithSkuStock(skuId);
                    stock.wareIds = wareIds;
                    stock.num = e.getCount();
                    return stock;
                })
                .collect(Collectors.toList());
        // 锁定库存
        boolean allStocked = true;
        for (SkuWareHasStock hasStock : skuWareHasStocks) {
            boolean skuStocked = false;
            Long skuId = hasStock.skuId;
            if (hasStock.wareIds == null || hasStock.wareIds.isEmpty()) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : hasStock.wareIds) {
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.num);
                if (count == 1) {
                    skuStocked = true;
                    break;
                } else {
                    // 当前仓库锁定失败，重试下一个仓库

                }
            }

            if (!skuStocked) {
                // 当前商品所有仓库都没锁住
                throw new NoStockException(skuId);

            }
        }
        return true;
    }

    class SkuWareHasStock {
        Long skuId;
        List<Long> wareIds;

        Integer num;
    }
}