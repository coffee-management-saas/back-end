package com.futurenbetter.saas.modules.chatbot.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "AI_chat_history")
public class AIChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @Column(columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;

    @Column(name = "retrieved_context", columnDefinition = "TEXT")
    private String retrievedContext;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
