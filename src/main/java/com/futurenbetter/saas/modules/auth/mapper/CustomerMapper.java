package com.futurenbetter.saas.modules.auth.mapper;

import com.futurenbetter.saas.modules.auth.dto.request.CustomerRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.request.UpdateProfileRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerRegistrationRequest request);

    @Mapping(target = "customerId", source = "id") // Tự động convert Long sang String
    @Mapping(target = "rankId", source = "membershipRank.id")
    @Mapping(target = "dob", source = "dob", dateFormat = "dd/MM/yyyy")
    CustomerResponse toResponse(Customer customer);

    void updateCustomerRequest(
            UpdateProfileRequest request,
            @MappingTarget Customer customer);
}
