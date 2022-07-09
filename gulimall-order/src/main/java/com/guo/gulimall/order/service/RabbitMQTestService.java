package com.guo.gulimall.order.service;


import com.guo.gulimall.order.entity.OrderEntity;
import com.guo.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RabbitListener(queues = {"hello-java-queue"})
public class RabbitMQTestService {

    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity entity, Channel channel) {
        log.info("收到消息....{}", entity);
    }

    @RabbitHandler
    public void receiveMessage(Message message, OrderEntity entity, Channel channel) {
        log.info("收到消息....{}", entity);
    }

}
