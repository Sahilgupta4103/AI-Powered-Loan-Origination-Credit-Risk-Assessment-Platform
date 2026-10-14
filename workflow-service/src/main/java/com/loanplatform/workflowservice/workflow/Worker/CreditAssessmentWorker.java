package com.loanplatform.workflowservice.workflow.Worker;

import com.loanplatform.workflowservice.client.AIServiceClient;
import com.loanplatform.workflowservice.client.LoanServiceClient;
import com.loanplatform.workflowservice.dto.LoanApplicationData;
import com.loanplatform.workflowservice.service.CreditRiskAssessmentService;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class CreditAssessmentWorker {

    private final LoanServiceClient loanServiceClient;
    private final CreditRiskAssessmentService creditRiskAssessmentService;
    private final AIServiceClient aiServiceClient;

    public CreditAssessmentWorker(LoanServiceClient loanServiceClient, CreditRiskAssessmentService creditRiskAssessmentService, AIServiceClient aiServiceClient) {
        this.loanServiceClient = loanServiceClient;
        this.creditRiskAssessmentService = creditRiskAssessmentService;
        this.aiServiceClient = aiServiceClient;
    }

    @JobWorker(type= "credit-assessment")
    public void assessCredit(JobClient client, ActivatedJob job){
        Long loanApplicationId = job.getVariablesAsMap().get("loanApplicationId") instanceof Number value ? value.longValue() : null;

        if (loanApplicationId == null) {
            throw new IllegalArgumentException(
                    "loanApplicationId is missing from workflow variables"
            );
        }

        System.out.println("Credit Assessment Started for: " + loanApplicationId);


        // Perform credit risk assessment
        LoanApplicationData application = loanServiceClient.getLoanApplication(loanApplicationId);
        if(application == null){
            throw new IllegalArgumentException(
                    "Loan application not found for ID: " + loanApplicationId
            );
        }

        Map<String, Object> assessmentResults = creditRiskAssessmentService.assess(application);
        String riskLevel = (String) assessmentResults.get("riskLevel");


        System.out.println("Credit assessment completed for ID: " + loanApplicationId + " with risk level: " + riskLevel);

        // Move loan to manager review
        if (!"MANAGER_REVIEW".equals(application.status())) {
            loanServiceClient.updateLoanStatus(
                    loanApplicationId,
                    "MANAGER_REVIEW"
            );
        }
        String summary = aiServiceClient.getAiSummary(loanApplicationId);

        Map<String,Object> variables = new HashMap<>(assessmentResults);
        variables.put("ai_summary", summary);
        client.newCompleteCommand(job).variables(variables).send().join();

        System.out.println("Credit assessment job completed.");
    }
}
