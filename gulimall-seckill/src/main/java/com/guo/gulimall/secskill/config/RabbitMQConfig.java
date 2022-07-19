package com.guo.gulimall.secskill.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Slf4j
@Configuration
public class RabbitMQConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {


        return new Jackson2JsonMessageConverter();
    }


    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 对象内部只有一个 id 属性，用来表示当前消息的唯一性
             * @param ack 对象内部只有一个 id 属性，用来表示当前消息的唯一性
             * @param cause 表示投递失败的原因。
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("confirm correlationData={}, ack={}, cause={}", correlationData, ack, cause);
            }
        });


        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            /**
             *
             * @param message the returned message.
             * @param replyCode the reply code.
             * @param replyText the reply text.
             * @param exchange the exchange.
             * @param routingKey the routing key.
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("未投递的消息:message={}, replyText={}, replyText={}, routingKey={}",
                        message, replyText, exchange, replyCode);
            }
        });
    }


}
