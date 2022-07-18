package com.guo.gulimall.secskill.scheduled;

import com.guo.gulimall.secskill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@EnableScheduling
public class SecKillScheduled {

    @Autowired
    SecKillService secKillService;

    @Autowired
    RedissonClient redissonClient;

    private final String uploadLock = "secKill: upload：lock";

    @Scheduled(cron = "0 * * * * *")
    public void uploadSecKillSkuLatest3Days() {

        RLock lock = redissonClient.getLock(uploadLock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            secKillService.uploadSecKillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
