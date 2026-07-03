package com.msq.gateway_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        return createFallbackResponse("Auth service is currently unavailable");
    }

    @GetMapping("/company")
    public ResponseEntity<Map<String, Object>> companyFallback() {
        return createFallbackResponse("Company/User service is currently unavailable");
    }

    @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> productFallback() {
        return createFallbackResponse("Product service is currently unavailable");
    }

    @GetMapping("/purchase")
    public ResponseEntity<Map<String, Object>> purchaseFallback() {
        return createFallbackResponse("Purchase service is currently unavailable");
    }

    @GetMapping("/sale")
    public ResponseEntity<Map<String, Object>> saleFallback() {
        return createFallbackResponse("Sale service is currently unavailable");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return createFallbackResponse("Payment service is currently unavailable");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
