package com.loanplatform.loanservice.service;

import com.loanplatform.loanservice.dto.CreateLoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanApplicationResponse;
import com.loanplatform.loanservice.dto.UpdateLoanApplicationRequest;
import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.enums.LoanApplicationStatus;
import com.loanplatform.loanservice.exception.LoanApplicationNotFoundException;
import com.loanplatform.loanservice.mapper.LoanApplicationMapper;
import com.loanplatform.loanservice.repository.LoanApplicationRepository;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository repository;
    private final LoanApplicationMapper mapper;
    private final LoanApplicationStatusTransitionService statusTransitionService;

    public LoanApplicationService(LoanApplicationRepository repository, LoanApplicationMapper mapper, LoanApplicationStatusTransitionService statusTransitionService){
        this.repository = repository;
        this.mapper = mapper;
        this.statusTransitionService = statusTransitionService;
    }

    @Transactional
    public LoanApplicationResponse createLoanApplication(CreateLoanApplicationRequest request){
        LoanApplication loanApplication = mapper.toEntity(request);
        loanApplication.setStatus(LoanApplicationStatus.DRAFT);
        loanApplication.setCreatedAt(LocalDateTime.now());
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication savedApplication = repository.save(loanApplication);
        return mapper.toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplication(Long id){
        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        return mapper.toResponse(loanApplication);
    }
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getLoanAppliations(LoanApplicationStatus status, Pageable pageable){
        Page<LoanApplication>applications;
        if(status!=null){
            applications = repository.findByStatus(status,pageable);
        }
        else{
            applications = repository.findAll(pageable);
        }
        return applications.map(mapper::toResponse);
    }

    @Transactional
    public LoanApplicationResponse updateLoanApplication(Long id, UpdateLoanApplicationRequest request){
        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        mapper.updateEntity(loanApplication,request);
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication updatedApplication = repository.save(loanApplication);
        return mapper.toResponse(updatedApplication);
    }

    @Transactional
    public LoanApplicationResponse submitLoanApplication(Long id){
        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        statusTransitionService.validTransition(loanApplication.getStatus(), LoanApplicationStatus.SUBMITTED);
        loanApplication.setStatus(LoanApplicationStatus.SUBMITTED);
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication submittedApplication = repository.save(loanApplication);
        return mapper.toResponse(submittedApplication);
    }

    @Transactional
    public LoanApplicationResponse updateStatus(Long id, LoanApplicationStatus requestedStatus){
        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        LoanApplicationStatus currentStatus = loanApplication.getStatus();
        statusTransitionService.validTransition(currentStatus, requestedStatus);
        loanApplication.setStatus(requestedStatus);
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication updatedLoanApplication = repository.save(loanApplication);
        return mapper.toResponse(updatedLoanApplication);
    }

}
