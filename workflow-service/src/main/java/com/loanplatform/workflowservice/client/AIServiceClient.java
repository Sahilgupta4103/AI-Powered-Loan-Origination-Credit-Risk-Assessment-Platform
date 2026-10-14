package com.loanplatform.workflowservice.client;

import com.loanplatform.workflowservice.dto.ManagerSummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AIServiceClient {

    private final RestClient restClient;

    public AIServiceClient(RestClient.Builder builder,@Value("${ai-service.base-url}") String baseUrl){
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String getAiSummary(long loanApplicationId){
        ManagerSummaryResponse response =  restClient.get()
                .uri("/api/v1/ai/loans/{loanApplicationId}/manager-summary", loanApplicationId)
                .retrieve()
                .body(ManagerSummaryResponse.class);
        if (response == null || response.summary() == null) {
            throw new IllegalStateException(
                    "AI service returned an empty summary for loan "
                            + loanApplicationId
            );
        }

        return response.summary();
    }

}
