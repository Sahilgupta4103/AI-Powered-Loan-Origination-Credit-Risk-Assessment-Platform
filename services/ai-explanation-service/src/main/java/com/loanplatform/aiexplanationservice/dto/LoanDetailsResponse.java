package com.loanplatform.aiexplanationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanDetailsResponse(
    Long id,
    String applicantName,
    String loanType,
    BigDecimal loanAmount,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Integer version,
    BigDecimal monthlyIncome,
    BigDecimal monthlyDebtObligations,
    String employmentType,
    Integer employmentDurationMonths
) {
}


