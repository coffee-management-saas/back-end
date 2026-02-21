package com.futurenbetter.saas.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@Configuration
public class AIConfig {

    @Bean
    public RestClientCustomizer aiRestClientCustomizer() {
        return restClientBuilder -> {
            restClientBuilder.messageConverters(converters -> {
                for (var converter : converters) {
                    if (converter instanceof MappingJackson2HttpMessageConverter jsonConverter) {
                        jsonConverter.setSupportedMediaTypes(List.of(
                                MediaType.APPLICATION_JSON,
                                MediaType.valueOf("application/octet-stream")
                        ));
                    }
                }
            });
        };
    }

    @Bean
    @Primary
    public ChatModel chatModel(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        return openAiChatModel;
    }

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(@Qualifier("ollamaEmbeddingModel") EmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }
}