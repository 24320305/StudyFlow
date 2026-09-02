package com.studyflow.backend.security;

import java.io.IOException;

import com.studyflow.backend.user.UserAccount;
import com.studyflow.backend.user.UserAccountRepository;
import com.studyflow.backend.user.UserStatus;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;
    private final TokenRevocationRepository tokenRevocationRepository;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserAccountRepository userAccountRepository,
            TokenRevocationRepository tokenRevocationRepository,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler) {
        this.jwtService = jwtService;
        this.userAccountRepository = userAccountRepository;
        this.tokenRevocationRepository = tokenRevocationRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveBearerToken(request);
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtToken token;
        try {
            token = jwtService.parse(accessToken);
        } catch (JwtException | IllegalArgumentException exception) {
            authenticationEntryPoint.write(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "Access token is invalid or expired");
            return;
        }

        if (tokenRevocationRepository.existsById(token.jti())) {
            authenticationEntryPoint.write(response, HttpServletResponse.SC_UNAUTHORIZED, "REVOKED_ACCESS_TOKEN", "Access token has been logged out");
            return;
        }

        UserAccount user = userAccountRepository.findById(token.userId()).orElse(null);
        if (user == null) {
            authenticationEntryPoint.write(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "Access token is no longer valid");
            return;
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            accessDeniedHandler.write(response, HttpServletResponse.SC_FORBIDDEN, "ACCOUNT_DISABLED", "This account has been disabled");
            return;
        }

        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
