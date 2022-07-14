package com.guo.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.to.SkuHasStockTo;
import com.guo.common.to.mq.OrderTO;
import com.guo.common.to.mq.StockLockedTO;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.ware.entity.WareSkuEntity;
import com.guo.gulimall.ware.vo.WareSkuLockVO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:26:17
 */
public interface WareSkuService extends IService<WareSkuEntity> {


    void unLockStock(StockLockedTO stockLockedTO);

    void unLockStock(OrderTO orderTO);

    PageUtils queryPage(Map<String, Object> params);

    public void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockTo> getSkuHasStock(List<Long> skuIds);

    boolean orderLockStock(WareSkuLockVO wareSkuLockVO);
}

