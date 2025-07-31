package com.example.mapsbridge.dto.response;

import com.example.mapsbridge.dto.MapType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseConvertResponse {
    /**
     * Map of links to different map providers.
     * Key: provider type (e.g., MapType.GOOGLE, MapType.APPLE)
     * Value: URL for that provider
     */
    protected Map<MapType, String> links;
}
