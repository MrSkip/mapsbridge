package com.example.mapsbridge.dto.response.shortcut;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

/**
 * Response model for the map link conversion API.
 * Contains the extracted location information and links to various map providers.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ShortcutResponse extends ShortcutBaseResponse {
    private Map<String, String> providers;

    public ShortcutResponse() {
        super(true);
    }
}
