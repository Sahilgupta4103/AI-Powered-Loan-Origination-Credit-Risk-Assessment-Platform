package com.loanplatform.workflowservice.service;

import com.loanplatform.workflowservice.client.LoanServiceClient;
import io.camunda.client.CamundaClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class WorkflowService {

    private final CamundaClient camundaClient;
    private final LoanServiceClient loanServiceClient;

    private WorkflowService(CamundaClient camundaClient, LoanServiceClient loanServiceClient){
        this.camundaClient = camundaClient;
        this.loanServiceClient = loanServiceClient;
    }

    public String StartLoanOriginationWorkflow(Long loanApplicationId){

        if(loanApplicationId == null || loanApplicationId <= 0){
            throw new IllegalArgumentException("Invalid loanApplicationId: " + loanApplicationId);
        }


        Map<String, Object> variables = new HashMap<>();
        variables.put("loanApplicationId", loanApplicationId);
        var result = camundaClient
                .newCreateInstanceCommand()
                .bpmnProcessId("loan-origination")
                .latestVersion()
                .variables(variables)
                .send()
                .join();
        return String.valueOf(result.getProcessInstanceKey());
    }

}
