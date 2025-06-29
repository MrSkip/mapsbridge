package com.example.mapsbridge.setup;

import org.springframework.http.HttpHeaders;

public class TestAuthUtils {
    
    public static final String TEST_MASTER_TOKEN = "test-master-token-123";
    
    public static HttpHeaders createAuthHeaders(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", key);
        return headers;
    }
    
    public static HttpHeaders createMasterAuthHeaders() {
        return createAuthHeaders(TEST_MASTER_TOKEN);
    }
}