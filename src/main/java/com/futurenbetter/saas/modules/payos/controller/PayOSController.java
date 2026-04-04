package com.futurenbetter.saas.modules.payos.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.modules.order.service.OrderService;
import com.futurenbetter.saas.modules.payos.service.inter.PayOSService;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PayOSController {
    private final PayOS payOS;
    private final PayOSService payOSService;
    private final OrderService orderService;
    private final SubscriptionService subscriptionService;

    // WEBHOOK
    @PostMapping(path = "/payos_transfer_handler")
    public ApiResponse<WebhookData> payosTransferHandler(@RequestBody Object body)
            throws JsonProcessingException, IllegalArgumentException {
        try {
            WebhookData data = payOS.webhooks().verify(body);

            if ((data.getOrderCode() / 1000000000000000L) == 1) {
                subscriptionService.updateSubscriptionStatus(data);
            } else {
                orderService.updateOrderStatus(data);
            }
            payOSService.messageReturnUrl(data);
            return ApiResponse.success(HttpStatus.OK, "Xác thực webhook thành công", data, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Xác thực webhook thất bại: " + e.getMessage(), null);
        }
    }
}
