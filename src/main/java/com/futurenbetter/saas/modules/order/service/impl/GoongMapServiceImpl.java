package com.futurenbetter.saas.modules.order.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.order.dto.response.GoongDistanceResponse;
import com.futurenbetter.saas.modules.order.service.GoongMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            if (response != null && !response.getRows().isEmpty() && !response.getRows().get(0).getElements().isEmpty()) {
                GoongDistanceResponse.Element element = response.getRows().get(0).getElements().get(0);
                if ("OK".equals(element.getStatus()) && element.getDistance() != null) {
                    double distanceKm = element.getDistance().getValue() / 1000.0;
                    return distanceKm;
                }
            }
        } catch (Exception e) {
            throw new BusinessException("Lỗi khi tính khoảng cách từ Goong API: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public long calculateShippingFee(double distanceKm) {
        if (distanceKm <= 0) return 0;
        
        long shippingFee;
        if (distanceKm <= 1.0) {
            shippingFee = 10000;
        } else {
            double extraKm = Math.ceil(distanceKm - 1.0);
            shippingFee = (long) (10000 + extraKm * 5000);
        }
        return shippingFee;
    }
}
