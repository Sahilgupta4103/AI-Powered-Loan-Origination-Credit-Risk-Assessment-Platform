package com.loanplatform.aiexplanationservice.client;

import com.loanplatform.aiexplanationservice.dto.LoanDetailsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LoanServiceClient {

    private final RestClient restClient;

    public LoanServiceClient(RestClient.Builder builder,@Value("${loan-service.base-url}") String baseUrl){
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public LoanDetailsResponse getLoanDetails(Long loanApplicationId) {
        return restClient.get()
                .uri("/api/v1/loans/{id}", loanApplicationId)
                .retrieve().body(LoanDetailsResponse.class);
    }

}
