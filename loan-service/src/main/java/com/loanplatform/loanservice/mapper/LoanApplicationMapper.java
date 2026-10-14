package com.loanplatform.loanservice.mapper;


import com.loanplatform.loanservice.dto.CreateLoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanApplicationResponse;
import com.loanplatform.loanservice.dto.UpdateLoanApplicationRequest;
import com.loanplatform.loanservice.entity.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationMapper {

    public LoanApplication toEntity(CreateLoanApplicationRequest request){
        LoanApplication loanApplication= new LoanApplication();
        loanApplication.setApplicantName(request.getApplicantName());
        loanApplication.setLoanType(request.getLoanType());
        loanApplication.setLoanAmount(request.getLoanAmount());
        loanApplication.setMonthlyIncome(request.getMonthlyIncome());
        loanApplication.setMonthlyDebtObligations(request.getMonthlyDebtObligations());
        loanApplication.setEmploymentType(request.getEmploymentType());
        loanApplication.setEmploymentDurationMonths(request.getEmploymentDurationMonths());
        return loanApplication;
    }

    public LoanApplicationResponse toResponse(LoanApplication entity){
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.setId(entity.getId());
        response.setApplicantName(entity.getApplicantName());
        response.setLoanType(entity.getLoanType());
        response.setLoanAmount(entity.getLoanAmount());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setVersion(entity.getVersion());
        response.setMonthlyIncome(entity.getMonthlyIncome());
        response.setMonthlyDebtObligations(entity.getMonthlyDebtObligations());
        response.setEmploymentType(entity.getEmploymentType());
        response.setEmploymentDurationMonths(entity.getEmploymentDurationMonths());
        return response;
    }

    public void updateEntity(LoanApplication entity, UpdateLoanApplicationRequest request){
        entity.setApplicantName(request.getApplicantName());
        entity.setLoanType(request.getLoanType());
        entity.setLoanAmount(request.getLoanAmount());
    }

}
