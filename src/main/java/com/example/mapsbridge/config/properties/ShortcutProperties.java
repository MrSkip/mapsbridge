package com.example.mapsbridge.config.properties;

import com.example.mapsbridge.dto.MapType;
import com.example.mapsbridge.dto.response.shortcut.ProviderItem;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Configuration properties for Shortcut responses.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "shortcut")
public class ShortcutProperties {

    private BadResponse badResponse = new BadResponse();
    private MapProviders mapProviders = new MapProviders();

    @Getter
    @Setter
    public static class BadResponse {
        private String alertTitle;
        private String alertMessage;
        private String url;
    }

    @Getter
    @Setter
    public static class MapProviders {
        private String order;
        private Map<String, String> displayValues = new HashMap<>();

        /**
         * Returns the ordered list of MapType enum values based on the configured order.
         *
         * @return List of MapType in the specified order
         */
        public List<MapType> getOrderedMapTypes() {
            if (order == null || order.isEmpty()) {
                return Arrays.asList(MapType.values());
            }

            return Arrays.stream(order.split(","))
                    .map(String::trim)
                    .map(MapType::valueOf)
                    .collect(Collectors.toList());
        }

        /**
         * Creates an ordered map based on the specified provider order.
         *
         * @param originalMap The original unordered map
         * @return A new LinkedHashMap with entries ordered according to the configured order
         */
        public <V> Map<MapType, V> createOrderedMap(Map<MapType, V> originalMap) {
            List<MapType> orderedTypes = getOrderedMapTypes();
            Map<MapType, V> orderedMap = new LinkedHashMap<>();

            // Add entries in the specified order
            for (MapType type : orderedTypes) {
                if (originalMap.containsKey(type)) {
                    orderedMap.put(type, originalMap.get(type));
                }
            }

            return orderedMap;
        }

        /**
         * Gets the display value for a given map provider type.
         * If no display value is configured, returns the provider's name.
         *
         * @param type The MapType to get the display value for
         * @return The display value string
         */
        public String getDisplayValue(MapType type) {
            return displayValues.getOrDefault(type.name(), type.getName());
        }

        /**
         * Creates a list of provider items with names and URLs, ordered according to the configured order.
         *
         * @param links Map of MapType to URL strings
         * @return A list of provider items with display names and URL strings
         */
        public List<ProviderItem> createProvidersMap(Map<MapType, String> links) {
            Map<MapType, String> orderedLinks = createOrderedMap(links);
            List<ProviderItem> providers = new ArrayList<>();

            for (Map.Entry<MapType, String> entry : orderedLinks.entrySet()) {
                String displayName = getDisplayValue(entry.getKey());
                providers.add(new ProviderItem(displayName, entry.getValue()));
            }

            return providers;
        }
    }
}