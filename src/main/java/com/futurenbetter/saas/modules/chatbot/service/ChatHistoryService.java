package com.futurenbetter.saas.modules.chatbot.service;

public interface ChatHistoryService {
    void savedHistory(String sessionId, String userMsg, String aiRes, String context);
    String getChatHistoryContext(String sessionId);

}
