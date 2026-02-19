package com.futurenbetter.saas.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class HttpClientConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(300000); // 5 minutes
            requestFactory.setReadTimeout(600000); // 10 minutes
            builder.requestFactory(requestFactory);
        };
    }
}
