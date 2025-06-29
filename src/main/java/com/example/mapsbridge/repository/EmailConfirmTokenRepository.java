package com.example.mapsbridge.repository;

import com.example.mapsbridge.model.EmailConfirmTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing EmailConfirmToken entities.
 */
@Repository
public interface EmailConfirmTokenRepository extends JpaRepository<EmailConfirmTokenModel, UUID> {

    /**
     * Find a token by its token string.
     *
     * @param token the token string
     * @return an Optional containing the token if found, empty otherwise
     */
    Optional<EmailConfirmTokenModel> findByToken(String token);

    /**
     * Find all tokens for a specific email.
     *
     * @param email the email address
     * @return a list of tokens associated with the email
     */
    List<EmailConfirmTokenModel> findByEmail(String email);

    /**
     * Find all tokens for a specific email that are not expired and not used.
     *
     * @param email the email address
     * @param now the current time to check against expiration
     * @return a list of valid tokens associated with the email
     */
    List<EmailConfirmTokenModel> findByEmailAndExpiresAtAfterAndUsedFalse(String email, LocalDateTime now);

    /**
     * Delete all expired tokens.
     *
     * @param expiryDate the date before which tokens are considered expired
     * @return the number of deleted tokens
     */
    long deleteByExpiresAtBefore(LocalDateTime expiryDate);
}