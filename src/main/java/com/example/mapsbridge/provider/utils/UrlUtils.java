package com.example.mapsbridge.provider.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlUtils {

    /**
     * URL-encodes a string value using UTF-8 encoding.
     *
     * @param value The string to encode
     * @return The URL-encoded string or empty string if input is null/empty
     */
    public static String encodeUrlParameter(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
