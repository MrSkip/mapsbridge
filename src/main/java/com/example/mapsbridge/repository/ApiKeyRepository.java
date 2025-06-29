package com.example.mapsbridge.repository;

import com.example.mapsbridge.model.ApiKeyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing ApiKey entities.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyModel, UUID> {

    /**
     * Find an API key by its value.
     *
     * @param apiKey the API key string
     * @return an Optional containing the API key if found, empty otherwise
     */
    Optional<ApiKeyModel> findByApiKey(String apiKey);

    /**
     * Check if an email already has an active API key.
     *
     * @param email the email address
     * @param active the active status
     * @return true if the email has an active API key, false otherwise
     */
    boolean existsByEmailAndActive(String email, boolean active);

    @Modifying
    @Transactional
    int removeByEmail(String email);
}