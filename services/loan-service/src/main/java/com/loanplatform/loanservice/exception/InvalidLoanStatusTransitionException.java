package com.loanplatform.loanservice.exception;

public class InvalidLoanStatusTransitionException extends RuntimeException{
    public InvalidLoanStatusTransitionException(String currentStatus, String requestedStatus){
        super("Invalid Loan status transition from "+ currentStatus + "to" + requestedStatus);
    }
}
