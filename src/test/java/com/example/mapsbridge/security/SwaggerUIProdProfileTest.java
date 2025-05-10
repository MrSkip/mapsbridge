package com.example.mapsbridge.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for Swagger UI access with prod profile
 * Swagger UI should NOT be accessible in prod profile
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
public class SwaggerUIProdProfileTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSwaggerUIIsNotAccessibleInProdProfile() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isForbidden());
    }
}