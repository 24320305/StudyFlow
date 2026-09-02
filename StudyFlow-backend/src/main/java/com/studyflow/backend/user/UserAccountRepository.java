package com.studyflow.backend.user;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT user FROM UserAccount user WHERE user.id = :id")
    Optional<UserAccount> findByIdForFollow(@Param("id") Long id);

    boolean existsByEmailIgnoreCase(String email);
}
