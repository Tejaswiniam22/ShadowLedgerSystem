package com.shadowledger.gateway.config;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

@Configuration
public class TraceIdFilter {

    @Bean
    public GlobalFilter traceIdGlobalFilter() {
        return (exchange, chain) -> {
            String traceId = UUID.randomUUID().toString();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-Trace-Id", traceId)
                            .header("X-Internal-Call", "true")
                    )
                    .build();

            return chain.filter(modifiedExchange);
        };
    }
}
