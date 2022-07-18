package com.guo.gulimall.secskill.service;

import com.guo.gulimall.secskill.to.SecKillSkuRedisTO;

import java.util.List;

public interface SecKillService {
    void uploadSecKillSkuLatest3Days();

    List<SecKillSkuRedisTO> getCurrentSecKill();

    SecKillSkuRedisTO getSkuSecKillInfo(Long skuId);
}
