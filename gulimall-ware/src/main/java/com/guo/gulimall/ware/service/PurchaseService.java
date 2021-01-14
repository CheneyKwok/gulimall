package com.guo.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guo.common.utils.PageUtils;
import com.guo.gulimall.ware.dto.MergeDto;
import com.guo.gulimall.ware.dto.PurchaseDoneDto;
import com.guo.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author guozhicheng
 * @email guozhicheng@gmail.com
 * @date 2020-12-26 17:26:17
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnReceive(Map<String, Object> params);

    void mergePurchase(MergeDto mergeDto);

    void received(List<Long> ids);

    public void done(PurchaseDoneDto doneDto);
}

