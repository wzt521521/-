package com.career.platform.openapi.repository;

import com.career.platform.openapi.entity.ApiKey;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByApiKeyHash(String apiKeyHash);

    Page<ApiKey> findByAppNameContainingIgnoreCase(String appName, Pageable pageable);

    @Modifying
    @Query("update ApiKey key set key.totalCalls = key.totalCalls + 1 where key.id = :id")
    int incrementTotalCalls(@Param("id") Long id);
}
