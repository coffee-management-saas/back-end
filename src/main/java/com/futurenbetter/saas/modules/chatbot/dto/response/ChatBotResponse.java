package com.futurenbetter.saas.modules.chatbot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatBotResponse {
    private String reply;
    private String conversationId;
    private String action; // INFO, COLLECTING, ORDER
    private Long orderId;
    private Boolean redirectToPayment;
}
