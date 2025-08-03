package com.example.mapsbridge.provider.extractor.komoot;

import com.example.mapsbridge.dto.LocationResult;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class K100DefaultExtractorTest {

    private K100DefaultExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new K100DefaultExtractor(new OkHttpClient.Builder().build());
    }

    @Test
    void testExtractFromDiscoverUrl() {
        // given
        String url = "https://www.komoot.com/discover/Location/@47.7955568,10.9346165/tours?sport=jogging&map=true&startLocation=47.7955568%2C10.9346165";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(47.7955568, result.getCoordinates().getLat());
        assertEquals(10.9346165, result.getCoordinates().getLon());
    }
}