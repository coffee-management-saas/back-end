package com.futurenbetter.saas.modules.chatbot.repository;

import com.futurenbetter.saas.modules.chatbot.entity.AIChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIChatHistoryRepository extends JpaRepository<AIChatHistory, Long> {
    List<AIChatHistory> findTop10BySessionIdOrderByCreatedAtDesc(String sessionId);
}
