package com.studyflow.backend.security;

import com.studyflow.backend.common.api.RequestIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Keep security filters inside the Spring Security chain only. */
@Configuration
public class FilterRegistrationConfig {

    @Bean
    FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration(RequestIdFilter filter) {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
