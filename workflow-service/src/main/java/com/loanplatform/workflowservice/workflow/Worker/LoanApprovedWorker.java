package com.loanplatform.workflowservice.workflow.Worker;

import com.loanplatform.workflowservice.client.LoanServiceClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;
import java.util.Map;
@Component
public class LoanApprovedWorker {
    private final LoanServiceClient loanServiceClient;

    public LoanApprovedWorker(LoanServiceClient loanServiceClient) {
        this.loanServiceClient = loanServiceClient;
    }

    @JobWorker(type = "loan-approved")
    public void approveLoan(JobClient client, ActivatedJob job){

        Object rawLoanId = job.getVariablesAsMap().get("loanApplicationId");
        Long loanApplicationId = rawLoanId instanceof Number ? ((Number) rawLoanId).longValue() : rawLoanId instanceof String ? Long.parseLong((String) rawLoanId) : null;

        if(loanApplicationId == null || loanApplicationId <= 0){
            throw new IllegalArgumentException("loanApplicationId is missing or invalid in the workflow variables");
        }
        System.out.println("Loan Approval Processing started for loanApplicationId : " + loanApplicationId);
        loanServiceClient.updateLoanStatus(
                loanApplicationId,
                "APPROVED"
        );


        System.out.println("Loan Approved for Application : " + loanApplicationId);
        client.newCompleteCommand(job).send().join();
        System.out.println("Loan Approval job completed");

    }

}
