package com.futurenbetter.saas.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.openai.chat.options.model}")
    private String GPT_OSS_120B;

    @Value("${spring.ai.openai.base-url}")
    private String CHAT_URL;

    @Value("${spring.ai.openai.api-key}")
    private String API_KEY_1;

    @Value("${spring.ai.openai.extend-key.key-2}")
    private String API_KEY_2;

    @Value("${spring.ai.openai.extend-key.key-3}")
    private String API_KEY_3;

    @Value("${spring.ai.openai.extend-key.key-4}")
    private String API_KEY_4;

    @Value("${spring.ai.openai.chat.options.temperature}")
    private double TEMPERATURE;

    @Bean
    public OpenAiApi apiKey1() {
        return OpenAiApi.builder()
                .apiKey(API_KEY_1)
                .baseUrl(CHAT_URL)
                .build();
    }

    @Bean
    public OpenAiApi apiKey2() {
        return OpenAiApi.builder()
                .apiKey(API_KEY_2)
                .baseUrl(CHAT_URL)
                .build();
    }

    @Bean
    public OpenAiApi apiKey3() {
        return OpenAiApi.builder()
                .apiKey(API_KEY_3)
                .baseUrl(CHAT_URL)
                .build();
    }

    @Bean
    public OpenAiApi apiKey4() {
        return OpenAiApi.builder()
                .apiKey(API_KEY_4)
                .baseUrl(CHAT_URL)
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }


    @Bean
    @Primary
    OpenAiChatModel modelKey1(@Qualifier(value = "apiKey1") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(GPT_OSS_120B)
                .temperature(TEMPERATURE)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    OpenAiChatModel modelKey2(@Qualifier(value = "apiKey2") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(GPT_OSS_120B)
                .temperature(TEMPERATURE)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    OpenAiChatModel modelKey3(@Qualifier(value = "apiKey3") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(GPT_OSS_120B)
                .temperature(TEMPERATURE)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    OpenAiChatModel modelKey4(@Qualifier(value = "apiKey4") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(GPT_OSS_120B)
                .temperature(TEMPERATURE)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    @Primary
    public ChatClient CLIENT_KEY_1(@Qualifier(value = "modelKey1") OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient CLIENT_KEY_2(@Qualifier(value = "modelKey2") OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient CLIENT_KEY_3(@Qualifier(value = "modelKey3") OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient CLIENT_KEY_4(@Qualifier(value = "modelKey4") OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
