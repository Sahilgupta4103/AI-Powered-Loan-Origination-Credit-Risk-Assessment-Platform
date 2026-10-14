package com.loanplatform.loanservice.controller;


import com.loanplatform.loanservice.dto.CreateLoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanApplicationResponse;
import com.loanplatform.loanservice.dto.UpdateLoanApplicationRequest;
import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.enums.LoanApplicationStatus;
import com.loanplatform.loanservice.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.*;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService){
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createLoanApplication(@Valid @RequestBody CreateLoanApplicationRequest request){
        LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getLoanApplication(@PathVariable Long id){
        LoanApplicationResponse response =  loanApplicationService.getLoanApplication(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<LoanApplicationResponse>> getLoanApplications(@RequestParam(required = false) LoanApplicationStatus status, Pageable pageable){
        Page<LoanApplicationResponse> response = loanApplicationService.getLoanAppliations(status,pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> updateLoanApplication(@PathVariable Long id, @Valid @RequestBody UpdateLoanApplicationRequest request){
        LoanApplicationResponse response = loanApplicationService.updateLoanApplication(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplicationResponse> submitLoanApplication(@PathVariable Long id){
        LoanApplicationResponse response =  loanApplicationService.submitLoanApplication(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LoanApplicationResponse> updateStatus(@PathVariable Long id, @RequestParam LoanApplicationStatus status){
        LoanApplicationResponse response = loanApplicationService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

}
