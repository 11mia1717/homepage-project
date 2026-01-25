package com.trustee.backend.auth.repository;

import com.trustee.backend.auth.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
}
