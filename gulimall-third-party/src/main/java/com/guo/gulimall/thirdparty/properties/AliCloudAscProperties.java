package com.guo.gulimall.thirdparty.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties("spring.cloud.alicloud")
public class AliCloudAscProperties {

    private String accessKey;

    private String secretKey;
}
