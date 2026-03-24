package com.futurenbetter.saas.modules.chatbot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotRequest {
    @JsonProperty("prompt")
    private String prompt;
    @JsonProperty("conversationId")
    private String conversationId;
}
