package com.guo.gulimall.order;

import com.guo.gulimall.order.entity.OrderEntity;
import com.guo.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;


@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {


	@Autowired
	AmqpAdmin amqpAdmin;

	@Autowired
	RabbitTemplate rabbitTemplate;

	/**
	 * 1. 如何创建 Exchange、Queue、Binding
	 * 1) 使用 AmqpAdmin 创建
	 */
	@Test
	void contextLoads() {
		createExchange();
		createQueue();
		createBinding();
		sendMessageTest();
	}


	@Test
	public void createExchange() {
		DirectExchange exchange = new DirectExchange("hello-java-exchange", true, false);
		amqpAdmin.declareExchange(exchange);
		log.info("Exchange[{}]创建成功", exchange.getName());
	}

	@Test
	public void createQueue() {
		Queue queue = new Queue("hello-java-queue", true, false, false);
		amqpAdmin.declareQueue(queue);
		log.info("Queue[{}]创建成功", queue.getName());
	}

	@Test
	public void createBinding() {
		Binding binding = new Binding("hello-java-queue",
				Binding.DestinationType.QUEUE,
				"hello-java-exchange",
				"hello.java",
				null);
		amqpAdmin.declareBinding(binding);
		log.info("binding[{}]创建成功", binding.toString());
	}

	@Test
	public void sendMessageTest() {

		for (int i = 0; i < 10; i++) {
			if (i % 2 == 0) {
				OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
				reasonEntity.setId(1L);
				reasonEntity.setCreateTime(new Date());
				reasonEntity.setName("HH" + i);
				rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
			} else {
				OrderEntity entity = new OrderEntity();
				entity.setOrderSn(UUID.randomUUID().toString());
				rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity);
			}
		}
	}



}
