package com.guo.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.fastjson.JSON;
import com.guo.common.excepiton.BizCodeEnum;
import com.guo.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class SentinelGatewayConfig implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
        BizCodeEnum codeEnum = BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION;
        R error = R.error(codeEnum.getCode(), codeEnum.getMsg());
        return ServerResponse.ok().body(Mono.just(JSON.toJSONString(error)), String.class);
    }
}
