package com.example.mapsbridge.config.auth.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class ApiKeyAuthToken extends AbstractAuthenticationToken {

    public static final String ROLE_MASTER = "ROLE_MASTER";
    public static final String ROLE_API_USER = "ROLE_API_USER";

    private final String apiKey;

    public ApiKeyAuthToken(String apiKey) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.apiKey = apiKey;
        setAuthenticated(false);
    }

    public ApiKeyAuthToken(String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
    }

    public static ApiKeyAuthToken createMasterToken(String apiKey) {
        return new ApiKeyAuthToken(apiKey, List.of(new SimpleGrantedAuthority(ROLE_MASTER)));
    }

    public static ApiKeyAuthToken createUserToken(String apiKey) {
        return new ApiKeyAuthToken(apiKey, List.of(new SimpleGrantedAuthority(ROLE_API_USER)));
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }
}
