package com.studyflow.backend.user;

import java.util.Locale;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;

import com.studyflow.backend.common.exception.BusinessException;
import com.studyflow.backend.security.AuthenticatedUser;
import com.studyflow.backend.security.JwtService;
import com.studyflow.backend.security.JwtToken;
import com.studyflow.backend.security.TokenRevocation;
import com.studyflow.backend.security.TokenRevocationRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final TokenRevocationRepository tokenRevocationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            TokenRevocationRepository tokenRevocationRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.tokenRevocationRepository = tokenRevocationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        validatePassword(request.password());
        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw BusinessException.conflict("EMAIL_ALREADY_EXISTS", "This email address is already registered");
        }

        UserAccount user = UserAccount.register(email, passwordEncoder.encode(request.password()), request.nickname().trim());
        try {
            userAccountRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw BusinessException.conflict("EMAIL_ALREADY_EXISTS", "This email address is already registered");
        }
        return issueToken(user);
    }

    public AuthTokenResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> BusinessException.unauthorized("INVALID_CREDENTIALS", "Email or password is incorrect"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("INVALID_CREDENTIALS", "Email or password is incorrect");
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw BusinessException.forbidden("ACCOUNT_DISABLED", "This account has been disabled");
        }
        return issueToken(user);
    }

    @Transactional
    public void logout(AuthenticatedUser currentUser, String authorizationHeader) {
        String accessToken = authorizationHeader.substring("Bearer ".length()).trim();
        JwtToken token;
        try {
            token = jwtService.parse(accessToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw BusinessException.unauthorized("INVALID_ACCESS_TOKEN", "Access token is invalid or expired");
        }
        if (!currentUser.id().equals(token.userId())) {
            throw BusinessException.forbidden("ACCESS_DENIED", "You cannot log out another user session");
        }
        if (!tokenRevocationRepository.existsById(token.jti())) {
            UserAccount user = userAccountRepository.getReferenceById(currentUser.id());
            tokenRevocationRepository.save(new TokenRevocation(token.jti(), user, token.expiresAt()));
        }
    }

    private AuthTokenResponse issueToken(UserAccount user) {
        JwtService.IssuedToken issuedToken = jwtService.issue(user);
        return new AuthTokenResponse(
                issuedToken.accessToken(),
                "Bearer",
                OffsetDateTime.ofInstant(issuedToken.expiresAt(), ZoneId.of("Asia/Shanghai")),
                UserProfileResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw BusinessException.badRequest("PASSWORD_TOO_LONG", "password must be at most 72 UTF-8 bytes");
        }
    }
}
