package com.studyflow.backend.security;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRevocationRepository extends JpaRepository<TokenRevocation, String> {
}
