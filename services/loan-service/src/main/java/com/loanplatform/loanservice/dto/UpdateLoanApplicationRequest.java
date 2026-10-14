package com.loanplatform.loanservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UpdateLoanApplicationRequest {

    @NotBlank(message = "Application name is required")
    private String applicantName;

    @NotBlank(message = "Loan type is required")
    private String loanType;

    @NotNull(message = "Loan Amount is required")
    @DecimalMin(value = "1000.00", message = "Loan Amount must be at least 1000")
    private BigDecimal loanAmount;

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }
}
