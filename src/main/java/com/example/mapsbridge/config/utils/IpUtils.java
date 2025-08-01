package com.example.mapsbridge.config.utils;

import jakarta.servlet.http.HttpServletRequest;

import static java.net.InetAddress.getByName;

public class IpUtils {
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "X-Original-Forwarded-For",
            "CF-Connecting-IP",
            "X-Cluster-Client-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = extractFromHeader(request, header);
            if (isValidIp(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    private static String extractFromHeader(HttpServletRequest request, String header) {
        String value = request.getHeader(header);
        if (value != null && !value.trim().isEmpty()) {
            return value.split(",")[0].trim();
        }
        return null;
    }

    private static boolean isValidIp(String ip) {
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        try {
            getByName(ip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
