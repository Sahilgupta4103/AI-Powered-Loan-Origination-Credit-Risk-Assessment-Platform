package com.loanplatform.workflowservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.*;

@Component
public class LoanServiceAuthClient {

    private final RestClient restClient;
    private final String username;
    private final String password;

    public LoanServiceAuthClient(@Value("${loan-service.base-url}") String baseUrl, @Value("${loan-service.auth.username}") String username, @Value("${loan-service.auth.password}") String password){
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.username = username;
        this.password = password;
    }

    public String getAccessToken() {
        Map<String, String> request =
                Map.of("username", username, "password", password);

        Map<?, ?> response = restClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null
                || !(response.get("accessToken") instanceof String token)
                || token.isBlank()) {
            throw new IllegalStateException(
                    "Login response did not contain a valid access token"
            );
        }

        return token;
    }
}
