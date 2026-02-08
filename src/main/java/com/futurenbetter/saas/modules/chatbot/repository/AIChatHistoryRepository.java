package com.futurenbetter.saas.modules.chatbot.repository;

import com.futurenbetter.saas.modules.chatbot.entity.AIChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIChatHistoryRepository extends JpaRepository<AIChatHistory, Long> {
}
