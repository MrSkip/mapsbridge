package com.example.mapsbridge.dto.response.shortcut;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Response model for the map link conversion API.
 * Contains the extracted location information and links to various map providers.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShortcutBadResponse extends ShortcutBaseResponse {
    private String alertTitle;
    private String alertMessage;
    private String url;

    public ShortcutBadResponse(String alertTitle, String alertMessage, String url) {
        super(false);
        this.alertTitle = alertTitle;
        this.alertMessage = alertMessage;
        this.url = url;
    }
}
