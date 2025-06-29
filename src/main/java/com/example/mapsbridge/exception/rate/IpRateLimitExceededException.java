package com.example.mapsbridge.exception.rate;

/**
 * Exception thrown when the rate limit for an IP address is exceeded.
 */
public class IpRateLimitExceededException extends RateLimitExceededException {
    
    private final String ip;
    
    public IpRateLimitExceededException(String ip) {
        super("Rate limit exceeded for IP: " + ip);
        this.ip = ip;
    }
    
    /**
     * Get the IP address that exceeded the rate limit.
     * 
     * @return the IP address
     */
    public String getIp() {
        return ip;
    }
}