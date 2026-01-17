package com.futurenbetter.saas.modules.subscription.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionPlanRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionPlanResponse;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionPlan;
import org.mapstruct.*;

import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionPlanMapper {

    ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "subscriptionPlanId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "shopSubscriptions", ignore = true)
    @Mapping(target = "configLimit", source = "configLimit", qualifiedByName = "mapToJsonString")
    SubscriptionPlan toEntity(SubscriptionPlanRequest request);


    @Mapping(target = "configLimit", source = "configLimit", qualifiedByName = "jsonStringToMap")
    SubscriptionPlanResponse toResponse(SubscriptionPlan entity);

    @Mapping(target = "subscriptionPlanId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "shopSubscriptions", ignore = true)
    @Mapping(target = "configLimit", source = "configLimit", qualifiedByName = "mapToJsonString")
    void updateEntityFromRequest(SubscriptionPlanRequest request, @MappingTarget SubscriptionPlan entity);


    //Chuyển đổi Map tính năng thành chuỗi Json lưu vào DB (jsonB)
    @Named("mapToJsonString")
    default String mapToJsonString(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    //Chuyển đổi chuỗi json từ DB thành Map để Frontend dễ dàng show các tính năng
    @Named("jsonStringToMap")
    default Map<String, Object> jsonStringToMap(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) return null;
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
