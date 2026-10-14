package com.loanplatform.loanservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateLoanApplicationRequest {

    @NotBlank(message = "Application name is required")
    private String applicantName;

    @NotBlank(message = "Loan type is required")
    private String loanType;

    @NotNull(message = "Loan Amount is required")
    @DecimalMin(value = "1000.00", message = "Loan Amount must be at least 1000")
    private BigDecimal loanAmount;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than zero")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Monthly debt obligations are required")
    @PositiveOrZero(message = "Monthly debt obligations cannot be negative")
    private BigDecimal monthlyDebtObligations;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    @NotNull(message = "Employment duration is required")
    @Min(value = 0, message = "Employment duration cannot be negative")
    private Integer employmentDurationMonths;



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

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getMonthlyDebtObligations() {
        return monthlyDebtObligations;
    }

    public void setMonthlyDebtObligations(BigDecimal monthlyDebtObligations) {
        this.monthlyDebtObligations = monthlyDebtObligations;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public Integer getEmploymentDurationMonths() {
        return employmentDurationMonths;
    }

    public void setEmploymentDurationMonths(Integer employmentDurationMonths) {
        this.employmentDurationMonths = employmentDurationMonths;
    }
}
