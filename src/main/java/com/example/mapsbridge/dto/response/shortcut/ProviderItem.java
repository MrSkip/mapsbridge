package com.example.mapsbridge.dto.response.shortcut;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a map provider with its name and URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderItem {
    private String name;
    private String url;
}