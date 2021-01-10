package com.guo.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * JSR303: Java规范提案第303号
 *  1）、给Bean添加校验注解：javax.validation.constraints,并定义自己的message提示
 *  2）、开启校验功能 @valid
 *  3）、给校验的bean后紧跟一个BingingResult，就可以获取校验的结果
 *  4）、分组校验（多场景复杂校验）
 *  	1）、@NotBlank等注解标注什么情况下进行校验
 * 		2）、@Validated(AddGroup.class)
 * 		3）、默认没有指定分组的校验注解@NotBlank，在分组校验的情况@Validated下不生效
 *	5）、自定义校验
 *		1）、编写一个自定义的校验注解 @ListValue
 *   	2）、编写一个自定义的校验器 ListValueValidator
 *  	3）、关联自定义的校验器和自定义的校验注解 @Constraint(validatedBy = { ListValueValidator.class })
 *  统一的异常处理 @ControllerAdvice
 *  1）、编写异常处理类，使用@ControllerAdvice
 *  2）、使用@ExceptionHandler标注方法可以处理的异常
 */
@EnableFeignClients //自动扫描此父包下的带有@FeignClient注解的类，或者手动指定路径
@MapperScan("com.guo.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallProductApplication.class, args);
	}

}
