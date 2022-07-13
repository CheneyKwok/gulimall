package com.guo.gulimall.ware.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;


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

    @Bean
    public Queue stockDelayQueue() {

        Map<String, Object> arguments = new HashMap<>();

        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue stockReleaseQueue() {
        return new Queue("stock.release.queue", true, false, false);
    }

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }


    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked", null);
    }


    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#", null);
    }

}
