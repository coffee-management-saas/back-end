package com.futurenbetter.saas.modules.chatbot.service.impl;

import com.futurenbetter.saas.modules.chatbot.entity.AIChatHistory;
import com.futurenbetter.saas.modules.chatbot.repository.AIChatHistoryRepository;
import com.futurenbetter.saas.modules.chatbot.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatHistoryServiceImpl implements ChatHistoryService {

    private final AIChatHistoryRepository chatHistoryRepository;

    @Override
    public void savedHistory(String sessionId, String userMsg, String aiRes, String context) {
        AIChatHistory history = AIChatHistory.builder()
                .sessionId(sessionId)
                .userMessage(userMsg)
                .aiResponse(aiRes)
                .retrievedContext(context)
                .createdAt(LocalDateTime.now())
                .build();
        chatHistoryRepository.save(history);
    }

    @Override
    public String getChatHistoryContext(String sessionId) {
        List<AIChatHistory> histories = chatHistoryRepository.findTop10BySessionIdOrderByCreatedAtDesc(sessionId);

        Collections.reverse(histories);

        return histories.stream()
                .map(h -> {
                    String userMsg = (h.getUserMessage() != null) ? h.getUserMessage() : "";
                    String aiRes = (h.getAiResponse() != null) ? h.getAiResponse() : "";

                    return String.format("Khách: %s\nAI: %s", userMsg, aiRes);
                })
                .collect(Collectors.joining("\n---\n"));
    }
}
