package com.futurenbetter.saas.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
            var httpClient = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofMinutes(2))
                    .build();

            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(java.time.Duration.ofMinutes(5));

            builder.requestFactory(requestFactory);
        };
    }
}
