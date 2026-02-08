package com.futurenbetter.saas.modules.chatbot.controller;

import com.futurenbetter.saas.modules.chatbot.dto.request.ChatRequest;
import com.futurenbetter.saas.modules.chatbot.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIChatService chatService;

    @PostMapping("/chat")
    String chat(ChatRequest request) {
        return chatService.chat(request);
    }
}
