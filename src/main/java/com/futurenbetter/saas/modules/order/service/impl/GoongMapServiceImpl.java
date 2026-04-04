package com.futurenbetter.saas.modules.order.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.order.dto.response.GoongDistanceResponse;
import com.futurenbetter.saas.modules.order.service.GoongMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoongMapServiceImpl implements GoongMapService {

    @Value("${goong.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public double getDistance(String origin, String destination) {
        String url = String.format(
                "https://rsapi.goong.io/DistanceMatrix?origins=%s&destinations=%s&vehicle=car&api_key=%s",
                origin, destination, apiKey
        );

        try {
            GoongDistanceResponse response = restTemplate.getForObject(url, GoongDistanceResponse.class);
            if (response != null && !response.getRows().isEmpty()) {
                return response.getRows().get(0).getElements().get(0).getDistance().getValue() / 1000.0;
            }
        } catch (Exception e) {
            throw new BusinessException("Lỗi khi tính khoảng cách từ Goong API");
        }
        return 0;
    }
}
