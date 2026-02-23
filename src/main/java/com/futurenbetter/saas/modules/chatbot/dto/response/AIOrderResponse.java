package com.futurenbetter.saas.modules.chatbot.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AIOrderResponse(
                String action,
                String message,
                OrderRequest orderRequest,
                Boolean redirectToPayment) {
}
