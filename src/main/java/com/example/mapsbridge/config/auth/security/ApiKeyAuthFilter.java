package com.example.mapsbridge.config.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ApiKeyAuthFilter extends AbstractAuthenticationProcessingFilter {
    private static final String API_KEY_HEADER = "X-API-Key";

    public ApiKeyAuthFilter(String... publicPaths) {
        super(createRequestMatcher(publicPaths));
        setAuthenticationSuccessHandler((request, response, authentication)
                -> SecurityContextHolder.getContext().setAuthentication(authentication));
    }

    private static RequestMatcher createRequestMatcher(String... publicPaths) {
        List<RequestMatcher> publicMatchers = Arrays.stream(publicPaths).map(AntPathRequestMatcher::new).collect(Collectors.toList());
        return new NegatedRequestMatcher(new OrRequestMatcher(publicMatchers));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        String requestURI = request.getRequestURI();

        log.info("Attempting authentication for URI: {}, API Key present: {}", requestURI, StringUtils.hasText(apiKey));

        if (!StringUtils.hasText(apiKey)) {
            log.warn("No API key provided for URI: {}", requestURI);
            throw new AuthenticationException("No API key provided") {
            };
        }

        ApiKeyAuthToken authRequest = new ApiKeyAuthToken(apiKey);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        log.info("Authentication successful for URI: {}, authorities: {}, continuing filter chain", request.getRequestURI(), authResult.getAuthorities());
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Log warning for all unauthorized access attempts
        String requestURI = request.getRequestURI();
        String apiKey = request.getHeader(API_KEY_HEADER);

        log.warn("Unsuccessful authentication for URI: {}, API Key present: {}, Exception: {}", requestURI, StringUtils.hasText(apiKey), failed != null ? failed.getMessage() : "No exception");
    }
}
