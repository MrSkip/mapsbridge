package com.example.mapsbridge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

/**
 * Entity representing an email confirmation token used for API key generation.
 * Tokens are sent to users via email and are valid for a limited time.
 */
@Entity
@Table(name = "email_confirm_tokens", indexes = {
        @Index(name = "idx_email_confirm_token_email", columnList = "email"),
        @Index(name = "idx_email_confirm_token", columnList = "token")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class EmailConfirmTokenModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Checks if the token is expired.
     *
     * @return true if the token is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now(UTC).isAfter(expiresAt);
    }

    /**
     * Checks if the token is valid (not expired and not used).
     *
     * @return true if the token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }
}