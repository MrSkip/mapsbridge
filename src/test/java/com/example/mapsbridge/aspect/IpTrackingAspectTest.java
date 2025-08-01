package com.example.mapsbridge.aspect;

import com.example.mapsbridge.config.metrics.tracker.IpAddressTracker;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IpTrackingAspectTest {

    @Mock
    private IpAddressTracker ipAddressTracker;

    @Mock
    private JoinPoint joinPoint;

    @InjectMocks
    private IpTrackingAspect ipTrackingAspect;

    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    public void testTrackIpAddress_DirectIp() {
        // Given
        String expectedIp = "192.168.1.1";
        request.setRemoteAddr(expectedIp);

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_XForwardedFor() {
        // Given
        String expectedIp = "203.0.113.195";
        request.addHeader("X-Forwarded-For", expectedIp);

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_XForwardedForWithMultipleIps() {
        // Given
        String expectedIp = "203.0.113.195";
        request.addHeader("X-Forwarded-For", expectedIp + ", 192.168.1.1, 10.0.0.1");

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_ProxyClientIp() {
        // Given
        String expectedIp = "203.0.113.195";
        request.addHeader("Proxy-Client-IP", expectedIp);

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_WLProxyClientIp() {
        // Given
        String expectedIp = "203.0.113.195";
        request.addHeader("WL-Proxy-Client-IP", expectedIp);

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_HttpClientIp() {
        // Given
        String expectedIp = "203.0.113.195";
        request.addHeader("HTTP_CLIENT_IP", expectedIp);

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_HttpXForwardedFor() {
        // Given
        String expectedIp = "203.0.113.195";
        request.addHeader("HTTP_X_FORWARDED_FOR", expectedIp);

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_FallbackOrder() {
        // Given
        String xForwardedIp = "10.0.0.1";
        String proxyClientIp = "10.0.0.2";
        String expectedIp = xForwardedIp; // Should use X-Forwarded-For

        request.addHeader("X-Forwarded-For", xForwardedIp);
        request.addHeader("Proxy-Client-IP", proxyClientIp);

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker).trackIpAddress(expectedIp);
    }

    @Test
    public void testTrackIpAddress_NoRequestAttributes() {
        // Given
        RequestContextHolder.resetRequestAttributes();

        // When
        ipTrackingAspect.trackIpAddress();

        // Then
        verify(ipAddressTracker, never()).trackIpAddress(anyString());
    }

    @Test
    public void testTrackIpAddress_ExceptionHandling() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(ipAddressTracker).trackIpAddress(anyString());

        // When
        ipTrackingAspect.trackIpAddress(); // Should not throw exception

        // Then - no exception thrown
        verify(ipAddressTracker).trackIpAddress(anyString());
    }
}