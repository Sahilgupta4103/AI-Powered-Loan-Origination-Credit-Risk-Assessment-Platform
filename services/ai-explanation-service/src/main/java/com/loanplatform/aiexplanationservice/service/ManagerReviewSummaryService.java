
package com.loanplatform.aiexplanationservice.service;

import com.loanplatform.aiexplanationservice.client.LoanServiceClient;
import com.loanplatform.aiexplanationservice.dto.LoanDetailsResponse;
import com.loanplatform.aiexplanationservice.dto.ManagerReviewSummaryResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ManagerReviewSummaryService {

    private final ChatClient chatClient;
    private final LoanServiceClient loanServiceClient;

    public ManagerReviewSummaryService(
            ChatClient.Builder chatClientBuilder,
            LoanServiceClient loanServiceClient) {
        this.chatClient = chatClientBuilder.build();
        this.loanServiceClient = loanServiceClient;
    }

    public ManagerReviewSummaryResponse generateSummary(
            Long loanApplicationId) {

        LoanDetailsResponse loan =
                loanServiceClient.getLoanDetails(loanApplicationId);

        String summary = chatClient.prompt()
                .system("""
                        You are an AI assistant supporting a loan manager.
                        Analyze the supplied loan application facts and write
                        a concise, professional preliminary review.

                        Rules:
                        1. Use only facts explicitly supplied in the input.
                        2. Never invent or infer missing financial values.
                        3. Treat each field according to its stated name.
                        4. Do not claim that data is corrupted or contradictory
                           unless the supplied values demonstrate that clearly.
                        5. Identify relevant missing information.
                        6. You may explain potential financial concerns only
                           when supported by the supplied facts.
                        7. Do not invent credit scores, credit history,
                           document verification, or risk assessments.
                        8. Do not change or misrepresent the recorded status.
                        9. Do not make the final lending decision.
                        10. Do not repeat every input field. Summarize only
                            relevant findings and their justification.
                        11. If the available data is insufficient, state that
                            a reliable risk assessment cannot yet be made.
                        12. Do not recommend REWORK, APPROVE, or REJECT
                            without sufficient evidence and an established
                            lending policy.

                        Format the response using these headings:
                        Profile Overview
                        Financial Observations
                        Missing Information
                        Preliminary Assessment

                        Keep the response concise and avoid repeating
                        the applicant's personal details unnecessarily.
                        """)
                .user("""
                        Review the following loan application.

                        Application ID: %s
                        Applicant: %s
                        Loan type: %s
                        Loan amount: %s
                        Recorded status: %s
                        Created at: %s
                        Last updated: %s
                        Version: %s
                        Monthly income: %s
                        Monthly debt obligations: %s
                        Employment type: %s
                        Employment duration in months: %s
                        """.formatted(
                        loan.id(),
                        loan.applicantName(),
                        loan.loanType(),
                        loan.loanAmount(),
                        loan.status(),
                        loan.createdAt(),
                        loan.updatedAt(),
                        loan.version(),
                        loan.monthlyIncome(),
                        loan.monthlyDebtObligations(),
                        loan.employmentType(),
                        loan.employmentDurationMonths()
                ))
                .call()
                .content();

        return new ManagerReviewSummaryResponse(
                loan.id(),
                summary
        );
    }
}
