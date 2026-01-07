package com.futurenbetter.saas.modules.auth.mapper;

import com.futurenbetter.saas.modules.auth.dto.request.CustomerRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerRegistrationRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "status", source = "customer.status")
    @Mapping(target = "rankId", source = "customer.membershipRank.id")
    CustomerResponse toResponse(Customer customer);
}
