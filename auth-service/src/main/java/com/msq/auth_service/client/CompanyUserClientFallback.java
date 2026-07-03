package com.msq.auth_service.client;


import com.msq.auth_service.dto.request.RegisterRequest;
import com.msq.auth_service.dto.response.ApiResponse;
import com.msq.auth_service.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class CompanyUserClientFallback implements CompanyUserClient {

    @Override
    public ApiResponse<UserResponse> registerCompany(RegisterRequest request) {
        return ApiResponse.error("Company-User service is currently unavailable");
    }
}