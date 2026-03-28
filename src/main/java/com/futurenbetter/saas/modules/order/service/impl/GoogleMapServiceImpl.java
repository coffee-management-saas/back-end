package com.futurenbetter.saas.modules.order.service.impl;

import com.futurenbetter.saas.modules.order.service.GoogleMapService;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMapServiceImpl implements GoogleMapService {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Override
    public Long calculateDistance(Double shopLat, Double shopLng, Double customerLat, Double customerLng) {
        if (shopLat == null || shopLng == null || customerLat == null || customerLng == null) {
            log.warn("Thiếu tọa độ, không thể tính khoảng cách");
            return 0L;
        }

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();

        try {
            DistanceMatrix result = DistanceMatrixApi.newRequest(context)
                    .origins(new LatLng(shopLat, shopLng))
                    .destinations(new LatLng(customerLat, customerLng))
                    .await();
            if (result != null && result.rows.length > 0 && result.rows[0].elements.length > 0) {
                return result.rows[0].elements[0].distance.inMeters;
            }
        } catch (Exception e) {
            log.error("Lỗi khi gọi Google Maps API: {}", e.getMessage());
        } finally {
            context.shutdown();
        }
        return 0L;
    }
}
