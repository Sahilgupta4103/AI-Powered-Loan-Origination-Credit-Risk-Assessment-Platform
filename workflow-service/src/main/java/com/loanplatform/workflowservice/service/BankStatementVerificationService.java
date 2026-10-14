package com.loanplatform.workflowservice.service;

import com.loanplatform.workflowservice.client.BankStatementAnalysisClient;
import com.loanplatform.workflowservice.client.LoanServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class BankStatementVerificationService {

    private final BankStatementPdfExtractor pdfExtractor;
    private final BankStatementAnalysisClient analysisClient;
    private final LoanAffordabilityService affordabilityService;
    private final LoanServiceClient loanServiceClient;

    public BankStatementVerificationService(
            BankStatementPdfExtractor pdfExtractor,
            BankStatementAnalysisClient analysisClient,
            LoanAffordabilityService affordabilityService,
            LoanServiceClient loanServiceClient) {
        this.pdfExtractor = pdfExtractor;
        this.analysisClient = analysisClient;
        this.affordabilityService = affordabilityService;
        this.loanServiceClient = loanServiceClient;
    }

    public Map<String, Object> extractAndAnalyze(
            MultipartFile file,
            Long loanApplicationId) throws IOException {
        String extractedText = pdfExtractor.extractText(file);
        return analyzeExtractedText(loanApplicationId, extractedText,
                file.getOriginalFilename());
    }

    public Map<String, Object> analyzeExtractedText(
            Long loanApplicationId,
            String extractedText,
            String fileName) {
        if (loanApplicationId == null || loanApplicationId <= 0) {
            throw new IllegalArgumentException(
                    "loanApplicationId is required for bank statement verification");
        }

        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException(
                    "Extracted bank statement text is required");
        }

        var analysis = analysisClient.analyze(extractedText);

        if (analysis == null
                || analysis.statementStartDate() == null
                || analysis.statementEndDate() == null
                || analysis.transactions() == null
                || analysis.transactions().isEmpty()) {
            throw new IllegalArgumentException(
                    "Complete bank statement analysis is required");
        }

        LocalDate start = LocalDate.parse(analysis.statementStartDate());
        LocalDate end = LocalDate.parse(analysis.statementEndDate());

        long months = ChronoUnit.MONTHS.between(
                YearMonth.from(start), YearMonth.from(end)) + 1;

        if (start.isAfter(end) || months != 6) {
            throw new IllegalArgumentException(
                    "Bank statement must cover exactly six calendar months");
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalExistingEmis = BigDecimal.ZERO;

        for (var transaction : analysis.transactions()) {
            if (transaction.date() == null
                    || transaction.amount() == null
                    || transaction.amount().signum() < 0) {
                throw new IllegalArgumentException(
                        "Invalid transaction data in bank statement analysis");
            }

            LocalDate transactionDate = LocalDate.parse(transaction.date());
            if (transactionDate.isBefore(start)
                    || transactionDate.isAfter(end)) {
                throw new IllegalArgumentException(
                        "Transaction falls outside the statement period");
            }

            if (!"CREDIT".equalsIgnoreCase(transaction.type())
                    && !"DEBIT".equalsIgnoreCase(transaction.type())) {
                throw new IllegalArgumentException(
                        "Unrecognized transaction type");
            }

            String category = transaction.category();
            if ("SALARY".equalsIgnoreCase(category)
                    && "CREDIT".equalsIgnoreCase(transaction.type())) {
                totalIncome = totalIncome.add(transaction.amount());
            } else if ("LIVING_EXPENSE".equalsIgnoreCase(category)
                    && "DEBIT".equalsIgnoreCase(transaction.type())) {
                totalExpenses = totalExpenses.add(transaction.amount());
            } else if ("EXISTING_EMI".equalsIgnoreCase(category)
                    && "DEBIT".equalsIgnoreCase(transaction.type())) {
                totalExistingEmis = totalExistingEmis.add(transaction.amount());
            }
        }

        BigDecimal divisor = BigDecimal.valueOf(6);
        BigDecimal averageMonthlyIncome =
                totalIncome.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal averageMonthlyExpenses =
                totalExpenses.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal averageExistingEmis =
                totalExistingEmis.divide(divisor, 2, RoundingMode.HALF_UP);

        var loanApplication =
                loanServiceClient.getLoanApplication(loanApplicationId);

        if (loanApplication == null || loanApplication.loanAmount() == null) {
            throw new IllegalArgumentException(
                    "Loan application or requested loan amount was not found");
        }

        var affordabilityResult = affordabilityService.assess(
                averageMonthlyIncome,
                averageMonthlyExpenses,
                averageExistingEmis,
                loanApplication.loanAmount());

        boolean loanEligible = "PASS".equalsIgnoreCase(affordabilityResult.decision());

        return Map.of(
                "loanApplicationId", loanApplicationId,
                "fileName", fileName,
                "analysis", analysis,
                "financialSummary", Map.of(
                        "averageMonthlyIncome", averageMonthlyIncome,
                        "averageMonthlyExpenses", averageMonthlyExpenses,
                        "averageExistingEmis", averageExistingEmis,
                        "requestedLoanAmount", loanApplication.loanAmount(),
                        "loanStatus", loanApplication.status()
                ),
                "affordabilityStatus", affordabilityResult.decision(),
                "affordabilityDecision", loanEligible ? "YES" : "NO",
                "decision", loanEligible ? "APPROVE" : "REJECT",
                "loanEligible", loanEligible,
                "message", loanEligible
                        ? "Affordability check passed. Loan continues to the credit assessment workflow."
                        : "Affordability check failed. Loan will be routed to the rejected flow."
        );
    }
}
