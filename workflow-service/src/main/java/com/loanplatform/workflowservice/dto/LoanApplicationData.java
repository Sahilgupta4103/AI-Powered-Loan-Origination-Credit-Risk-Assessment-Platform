
package com.loanplatform.workflowservice.dto;

import java.math.BigDecimal;

public record LoanApplicationData(
        Long id,
        String applicantName,
        String loanType,
        BigDecimal loanAmount,
        String status,
        BigDecimal monthlyIncome,
        BigDecimal monthlyDebtObligations,
        String employmentType,
        Integer employmentDurationMonths
) {
}
