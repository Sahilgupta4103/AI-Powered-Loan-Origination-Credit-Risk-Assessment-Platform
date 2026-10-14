package com.loanplatform.loanservice.dto;

import com.loanplatform.loanservice.enums.LoanApplicationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanApplicationResponse {

    private Long id;
    private String applicantName;
    private String loanType;
    private BigDecimal loanAmount;
    private LoanApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyDebtObligations;
    private String employmentType;
    private Integer employmentDurationMonths;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LoanApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(LoanApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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