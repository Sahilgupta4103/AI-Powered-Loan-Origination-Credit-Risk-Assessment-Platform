package com.loanplatform.workflowservice.workflow.Worker;

import com.loanplatform.workflowservice.client.LoanServiceClient;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoanRejectedWorker {
    private final LoanServiceClient loanServiceClient;

    public LoanRejectedWorker(LoanServiceClient loanServiceClient) {
        this.loanServiceClient = loanServiceClient;
    }

    @JobWorker(type = "loan-rejected")
    public void rejectLoan(JobClient client, ActivatedJob job){

        Object rawLoanId = job.getVariablesAsMap().get("loanApplicationId");
        Long loanApplicationId = rawLoanId instanceof Number ? ((Number) rawLoanId).longValue() : rawLoanId instanceof String ? Long.parseLong((String) rawLoanId) : null;

        System.out.println("Loan Rejection Processing started for loanApplicationId : " + loanApplicationId);
        if(loanApplicationId ==  null || loanApplicationId <= 0){
            throw new IllegalArgumentException("loanApplicationId is missing  or wrong in the workflow variables");
        }
        loanServiceClient.updateLoanStatus(
                loanApplicationId,
                "REJECTED"
        );

        System.out.println("Loan Rejected for Application : " + loanApplicationId);
        client.newCompleteCommand(job).send().join();
        System.out.println("Loan Rejection job completed");
    }

}
