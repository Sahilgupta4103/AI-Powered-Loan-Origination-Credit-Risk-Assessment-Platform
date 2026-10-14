package com.loanplatform.loanservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter =
                new JwtAuthenticationConverter();

        jwtAuthenticationConverter
                .setJwtGrantedAuthoritiesConverter(jwt -> {

                    String role = jwt.getClaimAsString("role");

                    if (role == null || role.isBlank()) {
                        return List.of();
                    }

                    return List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + role
                            )
                    );
                });

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                        .authorizeHttpRequests(auth -> auth

                                // Public endpoints
                                .requestMatchers(
                                        "/api/v1/loans/**",
                                        "/actuator/**",
                                        "/api/v1/auth/**",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/actuator/health"
                                ).permitAll()

                                // Loan application access
                                .requestMatchers(HttpMethod.POST, "/api/v1/loans")
                                .hasAnyRole("CUSTOMER_SERVICE", "ADMIN")

                                .requestMatchers(HttpMethod.GET, "/api/v1/loans/**")
                                .hasAnyRole(
                                        "CUSTOMER_SERVICE",
                                        "CREDIT_ANALYST",
                                        "MANAGER",
                                        "ADMIN"
                                )

                                .requestMatchers(HttpMethod.PUT, "/api/v1/loans/**")
                                .hasAnyRole("CUSTOMER_SERVICE", "ADMIN")

                                // Workflow/status operations
                                .requestMatchers(HttpMethod.PATCH, "/api/v1/loans/*/status")
                                .hasAnyRole(
                                        "CREDIT_ANALYST",
                                        "MANAGER",
                                        "ADMIN",
                                        "WORKFLOW_SERVICE"
                                )

                                // Submit application
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/loans/*/submit"
                                )
                                .hasAnyRole("CUSTOMER_SERVICE", "ADMIN")

                                // Anything else requires authentication
                                .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}