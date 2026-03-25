package com.futurenbetter.saas.modules.chatbot.service;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatBotRequest;
import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;

import com.futurenbetter.saas.modules.chatbot.dto.response.ChatBotResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface AIChatService {
    ChatBotResponse chat(ChatBotRequest request);
}
