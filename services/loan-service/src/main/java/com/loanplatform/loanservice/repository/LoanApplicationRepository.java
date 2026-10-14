package com.loanplatform.loanservice.repository;

import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.enums.LoanApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    Page<LoanApplication>findByStatus(LoanApplicationStatus status, Pageable pageable);
}
