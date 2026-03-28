package com.futurenbetter.saas.modules.order.service;

public interface GoogleMapService {
    Long calculateDistance(Double shopLat, Double shopLng, Double customerLat, Double customerLng);
}
