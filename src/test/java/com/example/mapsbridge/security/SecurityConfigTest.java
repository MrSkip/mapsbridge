package com.example.mapsbridge.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.mapsbridge.model.ConvertRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.security.token}")
    private String apiToken;

    @Test
    void testConvertEndpointIsPublic() throws Exception {
        ConvertRequest request = new ConvertRequest("40.6892,-74.0445");

        mockMvc.perform(post("/api/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testActuatorEndpointRequiresAuthentication() throws Exception {
        // Without API key - should be forbidden
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isForbidden());

        // With invalid API key - should be forbidden
        mockMvc.perform(get("/actuator/health")
                .header("X-API-KEY", "invalid-token"))
                .andExpect(status().isForbidden());

        // With valid API key - should be authorized
        mockMvc.perform(get("/actuator/health")
                .header("X-API-KEY", apiToken))
                .andExpect(status().isOk());
    }

    // Swagger UI access tests moved to dedicated test classes:
    // - SwaggerUIAccessTest (dev profile)
    // - SwaggerUILocalProfileTest (local profile)
    // - SwaggerUIProdProfileTest (prod profile)
}
