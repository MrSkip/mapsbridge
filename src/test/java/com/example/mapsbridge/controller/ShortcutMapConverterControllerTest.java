package com.example.mapsbridge.controller;

import com.example.mapsbridge.dto.request.ConvertRequest;
import com.example.mapsbridge.dto.response.shortcut.ProviderItem;
import com.example.mapsbridge.dto.response.shortcut.ShortcutBaseResponse;
import com.example.mapsbridge.dto.response.shortcut.ShortcutResponse;
import com.example.mapsbridge.service.converter.MapConverterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShortcutMapConverterControllerTest {

    @Mock
    private MapConverterService<ShortcutBaseResponse> mapConverterService;

    @InjectMocks
    private ShortcutMapConverterController controller;

    @Test
    public void testShortcutConvert_ReturnsProvidersAsArray() {
        // Arrange
        ConvertRequest request = new ConvertRequest();
        request.setInput("48.0839646,10.8589516");

        ShortcutResponse mockResponse = new ShortcutResponse();
        List<ProviderItem> providers = Arrays.asList(
                new ProviderItem("Google Maps", "https://www.google.com/maps?q=48.0839646,10.8589516"),
                new ProviderItem("Waze", "https://waze.com/ul?ll=48.0839646,10.8589516&navigate=yes")
        );
        mockResponse.setProviders(providers);

        when(mapConverterService.convert(any(ConvertRequest.class))).thenReturn(mockResponse);

        // Act
        ShortcutBaseResponse response = controller.shortcutConvert(request);

        // Assert
        assertInstanceOf(ShortcutResponse.class, response);
        ShortcutResponse shortcutResponse = (ShortcutResponse) response;
        assertEquals(2, shortcutResponse.getProviders().size());
        assertEquals("Google Maps", shortcutResponse.getProviders().get(0).getName());
        assertEquals("https://www.google.com/maps?q=48.0839646,10.8589516", shortcutResponse.getProviders().get(0).getUrl());
        assertEquals("Waze", shortcutResponse.getProviders().get(1).getName());
        assertEquals("https://waze.com/ul?ll=48.0839646,10.8589516&navigate=yes", shortcutResponse.getProviders().get(1).getUrl());
    }
}