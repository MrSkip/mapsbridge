package com.example.mapsbridge.provider.extractor.impl;

import com.example.mapsbridge.dto.Coordinate;
import com.example.mapsbridge.dto.LocationResult;
import com.example.mapsbridge.provider.extractor.google.G3AtSymbolExtractor;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class G3AtSymbolExtractorTest {

    private G3AtSymbolExtractor extractor;

    @Mock
    private Counter counter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        extractor = new G3AtSymbolExtractor();
    }


    @Test
    void shouldExtractCoordinatesFromUrlWithAtSymbolPattern() {
        // given
        // This URL contains coordinates in the format @LAT,LON
        String url = "https://www.google.com/maps/place/New+York/@40.7127753,-74.0059728,12z";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(40.7127753, coordinate.getLat());
        assertEquals(-74.0059728, coordinate.getLon());
        assertEquals("New York", result.getPlaceName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://www.google.com/maps/place/New+York/@40.7127753,-74.0059728,12z #New York",
            "https://www.google.com/maps/place/San+Francisco/@37.7749,-122.4194,15z #San Francisco",
            "https://www.google.com/maps/place/Berlin/@52.5200,13.4050,11z #Berlin",
            "https://www.google.com/maps/place/Times+Square/@40.7580,-73.9855,16z #Times Square",
            "https://www.google.com/maps/place/Eiffel+Tower/@48.8584,2.2945,17z #Eiffel Tower",
            "https://www.google.com/maps/place/Sydney+Opera+House/@-33.8568,151.2153,17z #Sydney Opera House",
            "https://www.google.com/maps/place/123+Main+Street/@40.7128,-74.0060,16z #123 Main Street",
            "https://www.google.com/maps/place/United+States+Capitol+Building/@38.8897,-77.0091,17z #United States Capitol Building",
            "https://www.google.com/maps/place/Central+Park+Zoo/@40.7678,-73.9718,17z #Central Park Zoo",
            "https://www.google.com/maps/place/McDonald's/@40.7128,-74.0060,16z #McDonald's"
    }, delimiter = '#')
    void shouldExtractPlaceNameFromUrl(String url, String expectedPlaceName) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        assertEquals(expectedPlaceName, result.getPlaceName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://www.google.com/maps/place/Caf%C3%A9+de+la+Paix/@48.8566,2.3522,17z #Café de la Paix",
            "https://www.google.com/maps/place/Times+Square+%26+42nd+Street/@40.7580,-73.9855,16z #Times Square & 42nd Street",
            "https://www.google.com/maps/place/M%C3%BCnchen/@48.1351,11.5820,11z #München",
            "https://www.google.com/maps/place/Champs-%C3%89lys%C3%A9es/@48.8698,2.3075,17z #Champs-Élysées",
            "https://www.google.com/maps/place/Toki%C5%8D/@35.6762,139.6503,17z #Tokiō",
            "https://www.google.com/maps/place/50%25+Off+Store/@40.7128,-74.0060,16z #50% Off Store"
    }, delimiter = '#')
    void shouldExtractPlaceNameWithSpecialCharacters(String url, String expectedPlaceName) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        assertEquals(expectedPlaceName, result.getPlaceName());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.google.com/maps/@40.7127753,-74.0059728,12z",
            "https://www.google.com/maps/search/@40.7127753,-74.0059728,12z",
            "https://www.google.com/maps/dir/@40.7127753,-74.0059728,12z",
            "https://www.google.com/maps/place//@40.7127753,-74.0059728,12z"
    })
    void shouldReturnNullPlaceNameWhenNotPresent(String url) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        if (result.hasValidCoordinates()) {
            assertNull(result.getPlaceName());
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://www.google.com/maps/place/Single/@40.7127753,-74.0059728,12z #Single",
            "https://www.google.com/maps/place/A/@40.7127753,-74.0059728,12z #A",
            "https://www.google.com/maps/place/1/@40.7127753,-74.0059728,12z #1",
            "https://www.google.com/maps/place/X+Y+Z/@40.7127753,-74.0059728,12z #X Y Z"
    }, delimiter = '#')
    void shouldHandleEdgeCasePlaceNames(String url, String expectedPlaceName) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNotNull(result);
        assertTrue(result.hasValidCoordinates());
        assertEquals(expectedPlaceName, result.getPlaceName());
    }


    @Test
    void shouldReturnNullForUrlWithoutPattern() {
        // given
        String url = "https://www.google.com/maps?q=New+York";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNull(result.getCoordinates());
    }

    @Test
    void shouldReturnNullForInvalidUrl() {
        // given
        String emptyUrl = "";
        String nullUrl = null;

        // when & then
        assertNull(extractor.extract(emptyUrl).getCoordinates());
        assertNull(extractor.extract(nullUrl).getCoordinates());
    }

    @Test
    void shouldHandleInvalidCoordinateFormat() {
        // given
        // This URL contains invalid coordinates in the @ pattern
        String url = "https://www.google.com/maps/place/Test/@invalid,coordinates,12z";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertNull(result.getCoordinates());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://www.google.com/maps/@0,0,15z 0.0 0.0",
            "https://www.google.com/maps/place/Tokyo/@35.6762,139.6503,12z 35.6762 139.6503",
            "https://www.google.com/maps/@-33.8688,151.2093,14z -33.8688 151.2093",
            "https://www.google.com/maps/@40.7128,-74.0060,10z 40.7128 -74.0060",
            "https://www.google.com/maps/@-89.9999,-179.9999,6z -89.9999 -179.9999",
            "https://www.google.com/maps/@90,180,4z 90.0 180.0",
            "https://www.google.com/maps/@37.7749123,-122.4194789,18z 37.7749123 -122.4194789",
            "https://www.google.com/maps/@.5,.5,15z 0.5 0.5",
            "https://maps.google.com/@48.8584,2.2945/data=!3m1!1e3 48.8584 2.2945",
            "https://www.google.com/maps/place/Eiffel+Tower/@48.8584,2.2945,15z/data=!4m5 48.8584 2.2945"
    }, delimiter = ' ')
    void shouldExtractVariousCoordinateFormats(String url, double expectedLat, double expectedLon) {
        // when
        LocationResult result = extractor.extract(url);

        // then
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(expectedLat, coordinate.getLat());
        assertEquals(expectedLon, coordinate.getLon());
    }

    @Test
    void shouldHandleVariousUrlFormats() {
        // No zoom parameter
        assertTrue(extractor.extract("https://www.google.com/maps/@41.40338,2.17403").hasValidCoordinates());

        // Integer zoom (no 'z')
        assertTrue(extractor.extract("https://www.google.com/maps/@55.7558,37.6173,11").hasValidCoordinates());

        // With search prefix
        assertTrue(extractor.extract("https://www.google.com/maps/search/coffee/@37.7749,-122.4194").hasValidCoordinates());

        // With empty dir prefix
        assertTrue(extractor.extract("https://www.google.com/maps/dir//@37.7749,-122.4194,12z").hasValidCoordinates());

        // With data parameter
        assertTrue(extractor.extract("https://www.google.com/maps/@48.8584,2.2945/data=!3m1!1e3").hasValidCoordinates());

        // Only the coordinate part
        assertTrue(extractor.extract("@40.7128,-74.0060").hasValidCoordinates());
    }

    @Test
    void shouldHandleInvalidCoordinateFormats() {
        // Multiple decimal points in lat
        assertNull(extractor.extract("https://www.google.com/maps/@12.34.56,78.90,15z").getCoordinates());

        // Multiple decimal points in lon
        assertNull(extractor.extract("https://www.google.com/maps/@12.34,78.90.12,15z").getCoordinates());

        // Missing latitude
        assertNull(extractor.extract("https://www.google.com/maps/@,78.90,15z").getCoordinates());

        // Incomplete longitude
        assertNull(extractor.extract("https://www.google.com/maps/@40.7128,-,15z").getCoordinates());

        // Text instead of numbers
        assertNull(extractor.extract("https://www.google.com/maps/@text,more-text,15z").getCoordinates());

        // Plus signs (not supported by pattern)
        assertNull(extractor.extract("https://www.google.com/maps/@+12.34,+78.90,15z").getCoordinates());
    }

    @Test
    void shouldHandleMultipleCoordinatesInUrl() {
        // given
        // URL with multiple @ coordinate patterns - should extract the first one
        String url = "https://www.google.com/maps/@40.7128,-74.0060,15z/place/@35.6762,139.6503,12z";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertTrue(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(40.7128, coordinate.getLat());
        assertEquals(-74.0060, coordinate.getLon());
    }

    @Test
    void shouldHandleExtremeValues() {
        // given
        // URL with very large coordinate values (outside normal range but valid for pattern)
        String url = "https://www.google.com/maps/@123456.789,-987654.321,15z";

        // when
        LocationResult result = extractor.extract(url);

        // then
        assertFalse(result.hasValidCoordinates());
        Coordinate coordinate = result.getCoordinates();
        assertEquals(123456.789, coordinate.getLat());
        assertEquals(-987654.321, coordinate.getLon());
    }
}
