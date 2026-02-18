package com.futurenbetter.saas.modules.chatbot.dto.response;

import com.futurenbetter.saas.modules.order.dto.request.OrderRequest;

public record AIOrderResponse(
        String action,
        String message,
        OrderRequest orderRequest
) {
}
