package com.guo.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 想要远程调用别的服务
 * 1）引入open-feign
 * 2）编写一个接口，告诉SpringCLoud这个接口需要调用远程服务，声明接口的每一个方法都会调用相应远程服务的请求
 * 3）开启远程调用功能 @EnableFeignClients
 */
@EnableRedisHttpSession
@EnableFeignClients("com.guo.gulimall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallMemberApplication.class, args);
	}

}
