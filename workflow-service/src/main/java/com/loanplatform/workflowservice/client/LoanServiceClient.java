package com.loanplatform.workflowservice.client;

import com.loanplatform.workflowservice.dto.LoanApplicationData;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LoanServiceClient {

    private final RestClient restClient;
    private final LoanServiceAuthClient authClient;

    public LoanServiceClient(@Value("${loan-service.base-url}") String baseUrl, LoanServiceAuthClient authClient){
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.authClient = authClient;
    }

    public void updateLoanStatus(Long loanApplicationId, String status){
        String accessToken = authClient.getAccessToken();
        restClient.patch()
                .uri(uriBuilder -> uriBuilder
                .path("/api/v1/loans/{loanApplicationId}/status")
                        .queryParam("status", status)
                .build(loanApplicationId))
                .header(HttpHeaders.AUTHORIZATION,"Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity();
    }

    public LoanApplicationData getLoanApplication(Long loanApplicationId){
        String accessToken = authClient.getAccessToken();
        return restClient.get()
                .uri("/api/v1/loans/{loanApplicationId}",loanApplicationId)
                .header(HttpHeaders.AUTHORIZATION,"Bearer " + accessToken)
                .retrieve()
                .body(LoanApplicationData.class);
    }

}
