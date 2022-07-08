package com.guo.gulimall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {


	@Autowired
	AmqpAdmin amqpAdmin;

	@Autowired
	RabbitTemplate rabbitTemplate;

	/**
	 * 1. 如何创建 Exchange、Queue、Binding
	 *    1) 使用 AmqpAdmin 创建
	 */
	@Test
	void contextLoads() {
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
		rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", "hello, world!");
	}


	@RabbitListener(queues = {"hello-java-queue"})
	public static void receiveMessageTest(Object message) {
		System.out.println(message);
	}


}
