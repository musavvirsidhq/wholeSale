package com.msq.auth_service.client;

import com.msq.auth_service.dto.request.RegisterRequest;
import com.msq.auth_service.dto.response.ApiResponse;
import com.msq.auth_service.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "COMPANY-USER-SERVICE", fallback = CompanyUserClientFallback.class)
public interface CompanyUserClient {

    @PostMapping("/api/companies/register-internal")
    ApiResponse<UserResponse> registerCompany(@RequestBody RegisterRequest request);
}