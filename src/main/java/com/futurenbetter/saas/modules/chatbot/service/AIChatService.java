package com.futurenbetter.saas.modules.chatbot.service;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;

public interface AIChatService {
    String chat(ChatRequest request);
}
