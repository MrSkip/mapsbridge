package com.example.mapsbridge.integrations;

import com.example.mapsbridge.dto.ConvertRequest;
import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.provider.MapProvider;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
    @Autowired
    private List<MapProvider> mapProviders;

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
                MapType source = MapType.fromString((String) testConfig.get("source"));

                LocationResult location = new LocationResult(source, expectedCoordinates, expectedAddress, expectedName);

                return new Object[]{input, location, description};
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }

    @ParameterizedTest(name = "{index}: {2}")
    @MethodSource("provideConvertTestData")
    void testConvertCoordinates(String input, LocationResult location, String testDescription) throws Exception {
        // Given
        ConvertRequest request = new ConvertRequest(input);
        // When/Then
        var resultActions = mockMvc.perform(post("/api/convert")
                        .headers(TestAuthUtils.createMasterAuthHeaders())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coordinates.lat").value(location.getCoordinates().getLat()))
                .andExpect(jsonPath("$.coordinates.lon").value(location.getCoordinates().getLon()))
                .andExpect(jsonPath("$.name").value(location.getPlaceName()))
                .andExpect(jsonPath("$.address").value(location.getAddress()));

        for (MapType value : MapType.values()) {
            if (value == location.getMapSource()) {
                resultActions.andExpect(jsonPath("$.links." + value.getName()).value(input));
                continue;
            }

            String expectedUrl = getExpectedUrl(value, location);
            resultActions.andExpect(jsonPath("$.links." + value.getName()).value(expectedUrl));
        }
    }

    private String getExpectedUrl(MapType mapType, LocationResult location) {
        for (MapProvider provider : mapProviders) {
            if (provider.getType().equals(mapType)) {
                return provider.generateUrl(location);
            }
        }
        return "";
    }
}