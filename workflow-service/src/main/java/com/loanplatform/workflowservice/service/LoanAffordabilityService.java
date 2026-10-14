
package com.loanplatform.workflowservice.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LoanAffordabilityService {

    private static  final Logger log = LoggerFactory.getLogger(LoanAffordabilityService.class);

    private static final BigDecimal MAX_EMI_RATIO =
            new BigDecimal("0.40");

    private static final BigDecimal MONTHLY_INTEREST_RATE =
            new BigDecimal("0.12")
                    .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);

    private static final int LOAN_TENURE_MONTHS = 60;

    public String evaluateEligibility(
            BigDecimal averageMonthlyIncome,
            BigDecimal averageMonthlyExpenses,
            BigDecimal averageExistingEmis,
            BigDecimal requestedLoanAmount) {
        return assess(
                averageMonthlyIncome,
                averageMonthlyExpenses,
                averageExistingEmis,
                requestedLoanAmount).decision().equalsIgnoreCase("PASS")
                ? "YES"
                : "NO";
    }

    public AffordabilityResult assess(
            BigDecimal averageMonthlyIncome,
            BigDecimal averageMonthlyExpenses,
            BigDecimal averageExistingEmis,
            BigDecimal requestedLoanAmount) {

        validateInputs(
                averageMonthlyIncome,
                averageMonthlyExpenses,
                averageExistingEmis,
                requestedLoanAmount);

        BigDecimal disposableIncome = averageMonthlyIncome
                .subtract(averageMonthlyExpenses)
                .subtract(averageExistingEmis);

        BigDecimal proposedEmi = calculateEmi(requestedLoanAmount);

        BigDecimal maximumAllowedEmi = disposableIncome
                .multiply(MAX_EMI_RATIO)
                .max(BigDecimal.ZERO);

        boolean passed = disposableIncome.signum() > 0
                && proposedEmi.compareTo(maximumAllowedEmi) <= 0;

        log.info("========== LOAN AFFORDABILITY ASSESSMENT ==========");
        log.info("Requested loan amount: {}", requestedLoanAmount);
        log.info("Annual interest rate: 12%");
        log.info("Loan tenure: {} months", LOAN_TENURE_MONTHS);

        log.info("Average monthly income: {}", averageMonthlyIncome);
        log.info("Average monthly expenses: {}", averageMonthlyExpenses);
        log.info("Average existing EMIs: {}", averageExistingEmis);
        log.info("Disposable monthly income: {}", disposableIncome);

        log.info("Proposed monthly EMI: {}", proposedEmi);
        log.info("Maximum allowed EMI (40% of disposable income): {}",
                maximumAllowedEmi);

        log.info("Affordability calculation: {} <= {}",
                proposedEmi, maximumAllowedEmi);

        log.info("Final affordability decision: {}",
                passed ? "PASS" : "FAIL");

        log.info("Decision reason: {}",
                passed
                        ? "Proposed EMI is within the configured affordability limit"
                        : "Disposable income is non-positive or proposed EMI exceeds the limit");

        log.info("====================================================");


        return new AffordabilityResult(
                passed ? "PASS" : "FAIL",
                averageMonthlyIncome,
                averageMonthlyExpenses,
                averageExistingEmis,
                disposableIncome,
                requestedLoanAmount,
                proposedEmi,
                maximumAllowedEmi,
                LOAN_TENURE_MONTHS,
                new BigDecimal("12.00"),
                passed
                        ? "Proposed EMI is within the configured affordability limit"
                        : "Proposed EMI exceeds the configured affordability limit"
        );
    }

    private BigDecimal calculateEmi(BigDecimal principal) {
        BigDecimal onePlusRate =
                BigDecimal.ONE.add(MONTHLY_INTEREST_RATE);

        double power = Math.pow(
                onePlusRate.doubleValue(),
                LOAN_TENURE_MONTHS);

        BigDecimal numerator = principal
                .multiply(MONTHLY_INTEREST_RATE)
                .multiply(BigDecimal.valueOf(power));

        BigDecimal denominator = BigDecimal.valueOf(power - 1.0);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private void validateInputs(
            BigDecimal income,
            BigDecimal expenses,
            BigDecimal existingEmis,
            BigDecimal loanAmount) {

        if (income == null || expenses == null
                || existingEmis == null || loanAmount == null) {
            throw new IllegalArgumentException(
                    "All affordability inputs are required");
        }

        if (income.signum() < 0 || expenses.signum() < 0
                || existingEmis.signum() < 0
                || loanAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Income, expenses and existing EMIs must be non-negative; "
                            + "loan amount must be positive");
        }
    }

    public record AffordabilityResult(
            String decision,
            BigDecimal averageMonthlyIncome,
            BigDecimal averageMonthlyExpenses,
            BigDecimal averageExistingEmis,
            BigDecimal disposableIncome,
            BigDecimal requestedLoanAmount,
            BigDecimal proposedEmi,
            BigDecimal maximumAllowedEmi,
            int tenureMonths,
            BigDecimal annualInterestRatePercent,
            String reason) {
    }
}
