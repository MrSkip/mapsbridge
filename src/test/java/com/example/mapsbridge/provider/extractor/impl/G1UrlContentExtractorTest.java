package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.LocationResult;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class G1UrlContentExtractorTest {

    @Mock
    private OkHttpClient httpClient;

    @Mock
    private Call call;

    @Mock
    private Response response;

    @Mock
    private ResponseBody responseBody;

    private G1UrlContentExtractor extractor;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // Configure mocks
        when(httpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);

        extractor = new G1UrlContentExtractor(httpClient);
    }

    @Test
    @DisplayName("Should return empty result when URL is blank")
    void shouldReturnEmptyResultWhenUrlIsBlank() {
        // Act
        LocationResult result = extractor.extract("  ");

        // Assert
        assertNotNull(result);
        assertNull(result.getCoordinates());
        assertNull(result.getAddress());
        assertNull(result.getPlaceName());
        verifyNoInteractions(httpClient);
    }

    @Test
    @DisplayName("Should handle HTTP failure gracefully")
    void shouldHandleHttpFailureGracefully() throws IOException {
        // Arrange
        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(404);

        // Act
        LocationResult result = extractor.extract("https://maps.google.com/test");

        // Assert
        assertNotNull(result);
        assertNull(result.getCoordinates());
        assertNull(result.getAddress());
        assertNull(result.getPlaceName());
    }

    @Test
    @DisplayName("Should handle IO exception gracefully")
    void shouldHandleIOExceptionGracefully() throws IOException {
        // Arrange
        when(call.execute()).thenThrow(new IOException("Network error"));

        // Act
        LocationResult result = extractor.extract("https://maps.google.com/test");

        // Assert
        assertNotNull(result);
        assertNull(result.getCoordinates());
        assertNull(result.getAddress());
        assertNull(result.getPlaceName());
    }

    @ParameterizedTest
    @CsvSource(value = {
//            "'<meta property=\"og:title\" content=\"Restaurant ABC · 123 Main St, New York\"/>',Restaurant ABC#123 Main St# New York",
            "'<meta itemprop=\"name\" content=\"Cafe XYZ • Downtown, Chicago\"/>'#Cafe XYZ#Downtown, Chicago",
//            "'<meta content=\"Hotel Resort · Beach Road, Miami\" property=\"og:title\"/>',Hotel Resort,Beach Road\\, Miami"
    }, delimiter = '#')
    @DisplayName("Should extract place name and address from meta tags")
    void shouldExtractPlaceNameAndAddressFromMetaTags(String htmlContent, String expectedPlaceName, String expectedAddress) throws IOException {
        // Arrange
        when(responseBody.string()).thenReturn(htmlContent);

        // Act
        LocationResult result = extractor.extract("https://maps.google.com/test");

        // Assert
        assertNotNull(result);
        assertEquals(expectedPlaceName, result.getPlaceName());
        assertEquals(expectedAddress, result.getAddress());
    }

    @ParameterizedTest
    @CsvSource({
            "'<div>Some content with @40.7128,-74.0060 coordinates</div>',40.7128,-74.0060",
            "'<span>Coordinates: @-33.8688,151.2093</span>',-33.8688,151.2093",
            "'<p>Visit us at @51.5074,-0.1278</p>',51.5074,-0.1278"
    })
    @DisplayName("Should extract coordinates from HTML content")
    void shouldExtractCoordinatesFromHtmlContent(String htmlContent, double expectedLat, double expectedLon) throws IOException {
        // Arrange
        when(responseBody.string()).thenReturn(htmlContent);

        // Act
        LocationResult result = extractor.extract("https://maps.google.com/test");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(expectedLat, result.getCoordinates().getLat());
        assertEquals(expectedLon, result.getCoordinates().getLon());
    }

    @ParameterizedTest
    @CsvSource({
            "'https://maps.google.com/maps?q=coffee&sll=@37.7749,-122.4194',37.7749,-122.4194",
            "'https://www.google.com/maps/place/Central+Park/@40.7812,-73.9665',40.7812,-73.9665",
//            "'https://goo.gl/maps/abc123@-22.9068,-43.1729',,-22.9068,-43.1729"
    })
    @DisplayName("Should extract coordinates from URL when not in content")
    void shouldExtractCoordinatesFromUrl(String url, double expectedLat, double expectedLon) throws IOException {
        // Arrange
        when(responseBody.string()).thenReturn("<html><body>No coordinates here</body></html>");

        // Act
        LocationResult result = extractor.extract(url);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCoordinates());
        assertEquals(expectedLat, result.getCoordinates().getLat());
        assertEquals(expectedLon, result.getCoordinates().getLon());

        // Verify URL was used for request
        ArgumentCaptor<okhttp3.Request> requestCaptor = ArgumentCaptor.forClass(okhttp3.Request.class);
        verify(httpClient).newCall(requestCaptor.capture());
        assertEquals(url, requestCaptor.getValue().url().toString());
    }

    @ParameterizedTest
    @CsvSource({
            "'<meta property=\"og:title\" content=\"Cafe &amp; Restaurant &lt;Famous&gt;\"/>',Cafe & Restaurant <Famous>,",
//            "'<meta content=\"Caf&eacute; &quot;Old Town&quot;\"/>',Café \"Old Town\",",
//            "'<meta content=\"Caf&eacute; &amp; Bakery &#39;Downtown&#39; &nbsp;\"/>',Café & Bakery 'Downtown' ,"
    })
    @DisplayName("Should decode HTML entities in content")
    void shouldDecodeHtmlEntitiesInContent(String htmlContent, String expectedDecodedName, String placeholder) throws IOException {
        // Arrange
        when(responseBody.string()).thenReturn(htmlContent);

        // Act
        LocationResult result = extractor.extract("https://maps.google.com/test");

        // Assert
        assertNotNull(result);
        assertEquals(expectedDecodedName, result.getPlaceName());
    }

    @Test
    @DisplayName("Should handle complete location data extraction")
    void shouldHandleCompleteLocationDataExtraction() throws IOException {
        // Arrange
        String htmlContent = "<meta property=\"og:title\" content=\"Central Park · Manhattan, NY\"/>" +
                "<div>Location: @40.7812,-73.9665</div>";
        when(responseBody.string()).thenReturn(htmlContent);

        // Act
        LocationResult result = extractor.extract("https://maps.google.com/centralpark");

        // Assert
        assertNotNull(result);
        assertEquals("Central Park", result.getPlaceName());
        assertEquals("Manhattan, NY", result.getAddress());
        assertNotNull(result.getCoordinates());
        assertEquals(40.7812, result.getCoordinates().getLat());
        assertEquals(-73.9665, result.getCoordinates().getLon());
    }

    @Test
    @DisplayName("Should handle null response body gracefully")
    void shouldHandleNullResponseBodyGracefully() throws IOException {
        // Arrange
        when(response.body()).thenReturn(null);

        // Act
        LocationResult result = extractor.extract("https://maps.google.com/test");

        // Assert
        assertNotNull(result);
        assertNull(result.getCoordinates());
        assertNull(result.getAddress());
        assertNull(result.getPlaceName());
    }
}
