package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;

    public AIChatServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String chat(ChatRequest request) {
        return chatClient
                .prompt(request.message())
                .call()
                .content();
    }
}
