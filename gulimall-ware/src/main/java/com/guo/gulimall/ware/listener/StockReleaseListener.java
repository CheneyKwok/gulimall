package com.guo.gulimall.ware.listener;


import com.guo.common.to.mq.OrderTO;
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
     * 自动解锁库存
     *
     * 下单成功，库存锁定成功，接下来的订单服务的业务调用失败，导致订单回滚，此时库存需要根据订单是否存在判断解锁
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTO stockLockedTO, Message message, Channel channel) throws IOException {

        try {
            log.info("收到解锁库存的消息");
            wareSkuService.unLockStock(stockLockedTO);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error(e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 被动解锁
     *
     * 下单成功，订单过期没有支付被系统自动取消或者被用户手动取消，订单关闭，订单服务主动发消息到 MQ 通知 库存服务解锁库存
     *
     */

    @RabbitHandler
    public void handleStockLockedRelease(OrderTO orderTO, Message message, Channel channel) throws IOException {

        try {
            log.info("收到解锁库存的消息，订单关闭通知");
            wareSkuService.unLockStock(orderTO);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error(e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
