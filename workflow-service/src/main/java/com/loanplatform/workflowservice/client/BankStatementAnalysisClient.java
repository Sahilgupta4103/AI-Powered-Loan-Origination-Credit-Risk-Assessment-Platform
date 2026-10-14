package com.loanplatform.workflowservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BankStatementAnalysisClient {

    private final RestClient restClient;

    public BankStatementAnalysisClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8083")
                .build();
    }

    public BankStatementAnalysisResponse analyze(String extractedText) {
        return restClient.post()
                .uri("/api/v1/ai/bank-statements/analyze")
                .body(new BankStatementAnalysisRequest(extractedText))
                .retrieve()
                .body(BankStatementAnalysisResponse.class);
    }

    public record BankStatementAnalysisRequest(String extractedText) {
    }

    public record BankStatementAnalysisResponse(
            String statementStartDate,
            String statementEndDate,
            java.util.List<TransactionClassification> transactions,
            java.util.List<String> dataQualityWarnings) {
    }

    public record TransactionClassification(
            String date,
            String description,
            java.math.BigDecimal amount,
            String type,
            String category,
            String evidence) {
    }
}