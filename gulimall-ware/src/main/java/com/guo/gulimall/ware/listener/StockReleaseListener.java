package com.guo.gulimall.ware.listener;


import com.guo.common.to.mq.StockLockedTO;
import com.guo.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = {"stock.release.queue"})
@Component
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 库存解锁场景
     * 1. 下单成功，订单过期没有支付被系统自动取消，被用户手动取消，解锁库存
     * 2. 下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTO stockLockedTO, Message message, Channel channel) throws IOException {

        try {
            log.info("收到解锁库存的消息");
            wareSkuService.unLockStock(stockLockedTO);
        } catch (Exception e) {
            log.error(e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
