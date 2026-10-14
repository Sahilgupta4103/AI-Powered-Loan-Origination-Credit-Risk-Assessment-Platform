package com.loanplatform.aiexplanationservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record BankStatementAnalysisResponse(
        String statementStartDate,
        String statementEndDate,
        List<TransactionClassification> transactions,
        List<String> dataQualityWarnings
) {
    public record TransactionClassification(
            String date,
            String description,
            BigDecimal amount,
            String type,
            String category,
            String evidence
    ) {
    }
}