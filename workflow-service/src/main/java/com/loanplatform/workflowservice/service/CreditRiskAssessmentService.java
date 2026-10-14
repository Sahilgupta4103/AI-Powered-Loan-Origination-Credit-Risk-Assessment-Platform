
package com.loanplatform.workflowservice.service;

import com.loanplatform.workflowservice.dto.LoanApplicationData;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CreditRiskAssessmentService {

    private static final BigDecimal LOW_EXISTING_DEBT_RATIO =
            new BigDecimal("20");

    private static final BigDecimal HIGH_EXISTING_DEBT_RATIO =
            new BigDecimal("35");

    private static final BigDecimal LOW_TOTAL_DEBT_RATIO =
            new BigDecimal("35");

    private static final BigDecimal HIGH_TOTAL_DEBT_RATIO =
            new BigDecimal("45");

    private static final int LOW_RISK_EMPLOYMENT_MONTHS = 24;
    private static final int HIGH_RISK_EMPLOYMENT_MONTHS = 6;

    // Initial project assumption, configurable later.
    private static final BigDecimal ANNUAL_INTEREST_RATE =
            new BigDecimal("12");

    private static final int LOAN_TENURE_MONTHS = 36;

    public Map<String, Object> assess(LoanApplicationData application) {

        List<String> reasons = new ArrayList<>();

        BigDecimal income = application.monthlyIncome();
        BigDecimal existingDebt = application.monthlyDebtObligations();
        BigDecimal loanAmount = application.loanAmount();
        Integer employmentMonths = application.employmentDurationMonths();

        if (income == null || income.signum() <= 0
                || existingDebt == null || existingDebt.signum() < 0
                || loanAmount == null || loanAmount.signum() <= 0
                || employmentMonths == null || employmentMonths < 0) {

            return Map.of(
                    "riskLevel", "INSUFFICIENT_DATA",
                    "assessmentSummary",
                    "Credit assessment could not be completed because "
                            + "required financial or employment data is missing "
                            + "or invalid.",
                    "riskReasons", List.of(
                            "Verify monthly income, existing debt obligations, "
                                    + "loan amount and employment duration."
                    )
            );
        }

        BigDecimal existingDebtRatio = existingDebt
                .multiply(BigDecimal.valueOf(100))
                .divide(income, 2, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment = calculateMonthlyPayment(loanAmount);

        BigDecimal totalMonthlyDebt = existingDebt.add(monthlyPayment);

        BigDecimal totalDebtRatio = totalMonthlyDebt
                .multiply(BigDecimal.valueOf(100))
                .divide(income, 2, RoundingMode.HALF_UP);

        boolean highRisk = false;
        boolean mediumRisk = false;

        if (existingDebtRatio.compareTo(HIGH_EXISTING_DEBT_RATIO) > 0) {
            highRisk = true;
            reasons.add("Existing debt-to-income ratio exceeds 35%.");
        } else if (existingDebtRatio.compareTo(LOW_EXISTING_DEBT_RATIO) > 0) {
            mediumRisk = true;
            reasons.add("Existing debt-to-income ratio is above 20%.");
        }

        if (totalDebtRatio.compareTo(HIGH_TOTAL_DEBT_RATIO) > 0) {
            highRisk = true;
            reasons.add("Total debt ratio after the proposed EMI exceeds 45%.");
        } else if (totalDebtRatio.compareTo(LOW_TOTAL_DEBT_RATIO) > 0) {
            mediumRisk = true;
            reasons.add("Total debt ratio after the proposed EMI exceeds 35%.");
        }

        if (employmentMonths < HIGH_RISK_EMPLOYMENT_MONTHS) {
            mediumRisk = true;
            reasons.add("Reported employment duration is under 6 months.");
        } else if (employmentMonths < LOW_RISK_EMPLOYMENT_MONTHS) {
            mediumRisk = true;
            reasons.add("Employment duration is under 24 months.");
        }

        String riskLevel = highRisk
                ? "HIGH"
                : mediumRisk ? "MEDIUM" : "LOW";

        if (reasons.isEmpty()) {
            reasons.add("All evaluated criteria fall within the configured "
                    + "low-risk ranges.");
        }

        String summary = switch (riskLevel) {
            case "HIGH" ->
                    "The application has been classified as high risk. "
                            + "One or more financial affordability thresholds "
                            + "have been exceeded. Manager review is required.";
            case "MEDIUM" ->
                    "The application has been classified as medium risk. "
                            + "Some affordability or employment criteria need "
                            + "further consideration during manager review.";
            default ->
                    "The application meets the configured low-risk criteria "
                            + "for the financial and employment data evaluated. "
                            + "The manager must still review the application.";
        };

        String allReasons = String.join(", ", reasons);

        return Map.of(
                "riskLevel", riskLevel,
                "applicantName", application.applicantName(),
                "loanAmount", loanAmount,
                "existingDebtRatio", existingDebtRatio,
                "estimatedMonthlyPayment", monthlyPayment,
                "totalDebtRatio", totalDebtRatio,
                "riskReasons", allReasons,
                "assessmentSummary", summary,
                "loanApplicationId", application.id()
        );
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal) {

        double monthlyRate = ANNUAL_INTEREST_RATE.doubleValue()
                / 100.0 / 12.0;

        double payment;

        if (monthlyRate == 0) {
            payment = principal.doubleValue() / LOAN_TENURE_MONTHS;
        } else {
            double factor = Math.pow(
                    1 + monthlyRate, LOAN_TENURE_MONTHS);

            payment = principal.doubleValue()
                    * monthlyRate * factor / (factor - 1);
        }

        return BigDecimal.valueOf(payment)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
