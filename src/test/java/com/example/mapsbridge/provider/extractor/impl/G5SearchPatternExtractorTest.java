
package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class G5SearchPatternExtractorTest {

    private G5SearchPatternExtractor extractor;

    @Mock
    private Counter.Builder counterBuilder;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configure mocks
        when(counterBuilder.tag(anyString(), anyString())).thenReturn(counterBuilder);
        when(counterBuilder.register(meterRegistry)).thenReturn(counter);

        extractor = new G5SearchPatternExtractor(counterBuilder, meterRegistry);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "https://www.google.com/maps/search/40.7128,-74.0060|40.7128|-74.0060",
            "https://www.google.com/maps/search/51.5074,-0.1278|51.5074|-0.1278",
            "https://www.google.com/maps/search/37.7749,-122.4194|37.7749|-122.4194",
            "https://www.google.com/maps/search/48.8566,2.3522|48.8566|2.3522",
            "https://www.google.com/maps/search/35.6762,139.6503|35.6762|139.6503",
            "https://www.google.com/maps/search/55.7558,37.6176|55.7558|37.6176",
            "https://www.google.com/maps/search/-33.8688,151.2093|-33.8688|151.2093",
            "https://www.google.com/maps/search/0.0,0.0|0.0|0.0",
            "https://www.google.com/maps/search/90.0,180.0|90.0|180.0",
            "https://www.google.com/maps/search/-90.0,-180.0|-90.0|-180.0"
    })
    void shouldExtractCoordinatesFromUrlWithSearchPattern(String url, double expectedLat, double expectedLon) {
        // when
        Coordinate coordinate = extractor.extract(url).getCoordinates();

        // then
        assertNotNull(coordinate);
        assertEquals(expectedLat, coordinate.getLat());
        assertEquals(expectedLon, coordinate.getLon());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.google.com/maps/place/New+York",
            "https://www.google.com/maps/dir/New+York/Boston",
            "https://www.google.com/maps/@40.7128,-74.0060,15z",
            "https://www.google.com/maps/place/New+York,+NY/@40.7128,-74.0060,15z",
            "https://www.google.com/maps/search/restaurants+near+me",
            "https://www.google.com/maps/search/hotels",
            "https://www.google.com/maps/search/coffee+shops",
            "https://www.google.com/maps/search/pizza",
            "https://www.google.com/maps/search/gas+stations",
            "https://www.google.com/maps/search/New+York+City",
            "https://www.google.com/maps/search/40.7128",
            "https://www.google.com/maps/search/,-74.0060",
            "https://www.google.com/maps/search/40.7128,"
    })
    void shouldReturnNullForUrlWithoutValidPattern(String url) {
        // when
        Coordinate coordinate = extractor.extract(url).getCoordinates();

        // then
        assertNull(coordinate);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "   ",
            "\t",
            "\n"
    })
    void shouldReturnNullForInvalidOrEmptyUrl(String url) {
        // when
        Coordinate coordinate = extractor.extract(url).getCoordinates();

        // then
        assertNull(coordinate);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.google.com/maps/search/invalid,coordinates",
            "https://www.google.com/maps/search/abc,def",
            "https://www.google.com/maps/search/40.7128,invalid",
            "https://www.google.com/maps/search/invalid,-74.0060",
            "https://www.google.com/maps/search/40.7128.123,-74.0060",
            "https://www.google.com/maps/search/40..7128,-74.0060",
            "https://www.google.com/maps/search/40.7128,-74..0060",
            "https://www.google.com/maps/search/40.7128x,-74.0060",
            "https://www.google.com/maps/search/40.7128,+-74.0060",
            "https://www.google.com/maps/search/40.7128,-+74.0060",
            "https://www.google.com/maps/search/40.7128,--74.0060",
            "https://www.google.com/maps/search/++40.7128,-74.0060"
    })
    void shouldHandleInvalidCoordinateFormat(String url) {
        // when
        Coordinate coordinate = extractor.extract(url).getCoordinates();

        // then
        assertNull(coordinate);
    }

    @Test
    void shouldReturnEmptyLocationResultForNullUrl() {
        // when
        var result = extractor.extract(null);

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldReturnEmptyLocationResultForEmptyUrl() {
        // when
        var result = extractor.extract("");

        // then
        assertNotNull(result);
        assertNull(result.getCoordinates());
    }
}