package com.loanplatform.loanservice.service;

import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.enums.LoanApplicationStatus;
import com.loanplatform.loanservice.exception.InvalidLoanStatusTransitionException;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class LoanApplicationStatusTransitionService {

    private final Map<LoanApplicationStatus, Set<LoanApplicationStatus>> allowedTransitions =
            Map.of(
              LoanApplicationStatus.DRAFT, Set.of(LoanApplicationStatus.SUBMITTED, LoanApplicationStatus.CANCELLED),
              LoanApplicationStatus.SUBMITTED, Set.of(LoanApplicationStatus.DOCUMENT_VERIFICATION, LoanApplicationStatus.CANCELLED),
              LoanApplicationStatus.DOCUMENT_VERIFICATION, Set.of(LoanApplicationStatus.CREDIT_ASSESSMENT, LoanApplicationStatus.REWORK),
              LoanApplicationStatus.CREDIT_ASSESSMENT, Set.of(LoanApplicationStatus.MANAGER_REVIEW, LoanApplicationStatus.REWORK),
                    LoanApplicationStatus.MANAGER_REVIEW, Set.of(LoanApplicationStatus.APPROVED, LoanApplicationStatus.REJECTED, LoanApplicationStatus.REWORK),
                    LoanApplicationStatus.REWORK, Set.of(LoanApplicationStatus.SUBMITTED),
                    LoanApplicationStatus.APPROVED, Set.of(),
                    LoanApplicationStatus.REJECTED, Set.of(),
                    LoanApplicationStatus.CANCELLED, Set.of()
            );

    public void validTransition(LoanApplicationStatus currentStatus, LoanApplicationStatus requestedStatus){
        Set<LoanApplicationStatus> allowedStatuses = allowedTransitions.getOrDefault(currentStatus, Set.of());
        if(!allowedStatuses.contains(requestedStatus)){
            throw new InvalidLoanStatusTransitionException(currentStatus.name(), requestedStatus.name());
        }
    }

}
