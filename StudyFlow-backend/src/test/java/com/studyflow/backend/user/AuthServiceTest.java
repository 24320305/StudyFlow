package com.studyflow.backend.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.studyflow.backend.common.exception.BusinessException;
import com.studyflow.backend.security.JwtService;
import com.studyflow.backend.security.TokenRevocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private TokenRevocationRepository tokenRevocationRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userAccountRepository,
                tokenRevocationRepository,
                new BCryptPasswordEncoder(),
                jwtService);
    }

    @Test
    void rejectsUnknownLoginEmailWithoutRevealingAccountState() {
        when(userAccountRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "password123")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email or password is incorrect");
    }

    @Test
    void registrationIgnoresClientSuppliedRoleBecauseRequestHasNoRoleField() {
        when(userAccountRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userAccountRepository.saveAndFlush(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.issue(any(UserAccount.class)))
                .thenReturn(new JwtService.IssuedToken("token", "jti", java.time.Instant.now().plusSeconds(3600)));

        AuthTokenResponse response = authService.register(new RegisterRequest("new@example.com", "password123", "New User"));

        org.assertj.core.api.Assertions.assertThat(response.user().role()).isEqualTo(UserRole.USER);
    }

    @Test
    void rejectsPasswordLongerThanBcryptByteLimit() {
        String multibytePassword = "密".repeat(25);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "new@example.com", multibytePassword, "New User")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("password must be at most 72 UTF-8 bytes");
    }
}
