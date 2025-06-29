package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.ConvertResponse;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.service.MailtrapService;
import com.example.mapsbridge.service.MapConverterService;
import com.example.mapsbridge.setup.TestAuthUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MapConverterControllerTest {

    @MockitoBean
    private MapConverterService mapConverterService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testConvertCoordinates() throws Exception {
        // Given
        ConvertRequest request = new ConvertRequest("40.6892,-74.0445");

        Coordinate coordinate = new Coordinate(40.6892, -74.0445);
        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=40.6892,-74.0445");
        links.put(MapType.APPLE, "https://maps.apple.com/?ll=40.6892,-74.0445");

        ConvertResponse response = new ConvertResponse();
        response.setCoordinates(coordinate);
        response.setLinks(links);

        when(mapConverterService.convert(any(ConvertRequest.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/convert")
                        .headers(TestAuthUtils.createMasterAuthHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coordinates.lat").value(40.6892))
                .andExpect(jsonPath("$.coordinates.lon").value(-74.0445))
                .andExpect(jsonPath("$.links.google").value("https://www.google.com/maps?q=40.6892,-74.0445"))
                .andExpect(jsonPath("$.links.apple").value("https://maps.apple.com/?ll=40.6892,-74.0445"));
    }

    @Test
    void testConvertGoogleMapsUrl() throws Exception {
        // Given
        ConvertRequest request = new ConvertRequest("https://maps.google.com/?q=Statue+of+Liberty");

        Coordinate coordinate = new Coordinate(40.6892, -74.0445);
        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=40.6892,-74.0445");
        links.put(MapType.APPLE, "https://maps.apple.com/?ll=40.6892,-74.0445");

        ConvertResponse response = new ConvertResponse();
        response.setCoordinates(coordinate);
        response.setLinks(links);

        when(mapConverterService.convert(any(ConvertRequest.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/convert")
                        .headers(TestAuthUtils.createMasterAuthHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coordinates.lat").value(40.6892))
                .andExpect(jsonPath("$.coordinates.lon").value(-74.0445))
                .andExpect(jsonPath("$.links.google").value("https://www.google.com/maps?q=40.6892,-74.0445"))
                .andExpect(jsonPath("$.links.apple").value("https://maps.apple.com/?ll=40.6892,-74.0445"));
    }

    @Test
    void testInvalidInput() throws Exception {
        // Given
        ConvertRequest request = new ConvertRequest("invalid input");

        when(mapConverterService.convert(any(ConvertRequest.class)))
                .thenThrow(new IllegalArgumentException("Input must be coordinates or a valid URL"));

        // When/Then
        mockMvc.perform(post("/api/convert")
                        .headers(TestAuthUtils.createMasterAuthHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Input must be coordinates or a valid URL"));
    }

    @Test
    void testEmptyInput() throws Exception {
        // Given
        ConvertRequest request = new ConvertRequest("");

        // When/Then
        mockMvc.perform(post("/api/convert")
                        .headers(TestAuthUtils.createMasterAuthHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}