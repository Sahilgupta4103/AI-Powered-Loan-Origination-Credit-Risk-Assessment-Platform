package com.loanplatform.workflowservice.dto;

public class StartLoanWorkflowRequest {

    private Long loanApplicationId;

    public  Long getLoanApplicationId(){
        return loanApplicationId;
    }
    public void setLoanApplicationId(Long loanApplicationId) {
        this.loanApplicationId = loanApplicationId;
    }
}
