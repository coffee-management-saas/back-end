package com.futurenbetter.saas.modules.chatbot.service;

import com.futurenbetter.saas.modules.chatbot.entity.AIChatHistory;
import com.futurenbetter.saas.modules.chatbot.repository.AIChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final AIChatHistoryRepository chatHistoryRepository;

    @Async
    public void saveHistory(String userMsg, String aiRes, String context) {
        AIChatHistory history = AIChatHistory.builder()
                .userMessage(userMsg)
                .aiResponse(aiRes)
                .retrievedContext(context)
                .createdAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(history);
    }
}
