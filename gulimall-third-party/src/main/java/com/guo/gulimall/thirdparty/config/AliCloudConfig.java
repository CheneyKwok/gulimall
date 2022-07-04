package com.guo.gulimall.thirdparty.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.guo.gulimall.thirdparty.properties.AliCloudAscProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(AliCloudAscProperties.class)
public class AliCloudConfig {

    @Bean("smsClient")
    public IAcsClient smsClient(AliCloudAscProperties ascProperties) throws ClientException {

        DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", ascProperties.getAccessKey(), ascProperties.getSecretKey());
        // 短信API产品名称
        final String product = "Dysmsapi";
        // 短信API产品域名
        final String domain = "dysmsapi.aliyuncs.com";
        DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", product, domain);
        return new DefaultAcsClient(profile);
    }
}
