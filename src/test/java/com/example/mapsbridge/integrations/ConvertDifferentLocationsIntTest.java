package com.example.mapsbridge.integrations;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.setup.TestAuthUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
//@Execution(ExecutionMode.SAME_THREAD)
class ConvertDifferentLocationsIntTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static Stream<Object[]> provideConvertTestData() {
        try {
            InputStream inputStream = ConvertDifferentLocationsIntTest.class.getResourceAsStream("/postman/postman-validation.json");
            List<Map<String, Object>> testCases = new ObjectMapper().readValue(inputStream,
                    new com.fasterxml.jackson.core.type.TypeReference<>() {
                    });

            return testCases.stream().map(testCase -> {
                String input = (String) testCase.get("input");
                Map<String, Object> expected = (Map<String, Object>) testCase.get("expected");
                Map<String, Object> coordinates = (Map<String, Object>) expected.get("coordinates");

                Double lat = ((Number) coordinates.get("lat")).doubleValue();
                Double lon = ((Number) coordinates.get("lon")).doubleValue();

                Coordinate expectedCoordinates = new Coordinate(lat, lon);

                String expectedName = (String) expected.get("name");
                String expectedAddress = (String) expected.get("address");

                Map<String, Object> testConfig = (Map<String, Object>) testCase.get("testConfig");
                String description = (String) testConfig.get("description");

                return new Object[]{input, expectedCoordinates, expectedName, expectedAddress, description};
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }

    @ParameterizedTest(name = "{index}: {4}")
    @MethodSource("provideConvertTestData")
    void testConvertCoordinates(String input, Coordinate expectedCoordinates, String expectedName, String expectedAddress, String testDescription) throws Exception {
        // Given
        ConvertRequest request = new ConvertRequest(input);

        Map<MapType, String> links = new HashMap<>();
        links.put(MapType.GOOGLE, "https://www.google.com/maps?q=" + expectedCoordinates.getLat() + "," + expectedCoordinates.getLon());
        links.put(MapType.APPLE, "https://maps.apple.com/?ll=" + expectedCoordinates.getLat() + "," + expectedCoordinates.getLon());

        // When/Then
        mockMvc.perform(post("/api/convert")
                        .headers(TestAuthUtils.createMasterAuthHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coordinates.lat").value(expectedCoordinates.getLat()))
                .andExpect(jsonPath("$.coordinates.lon").value(expectedCoordinates.getLon()))
                .andExpect(jsonPath("$.name").value(expectedName))
                .andExpect(jsonPath("$.address").value(expectedAddress))
                .andExpect(jsonPath("$.links.google").value("https://www.google.com/maps?q=" + expectedCoordinates.getLat() + "," + expectedCoordinates.getLon()))
                .andExpect(jsonPath("$.links.apple").value(startsWith("https://maps.apple.com/place?ll=" + expectedCoordinates.getLat() + "," + expectedCoordinates.getLon())));
    }
}