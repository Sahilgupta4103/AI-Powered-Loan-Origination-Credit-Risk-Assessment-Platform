package com.loanplatform.workflowservice.controller;

import com.loanplatform.workflowservice.service.BankStatementVerificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bank-statements")
public class BankStatementController {

    private final BankStatementVerificationService bankStatementVerificationService;

    public BankStatementController(
            BankStatementVerificationService bankStatementVerificationService) {
        this.bankStatementVerificationService = bankStatementVerificationService;
    }

    @PostMapping(
            value = "/extract",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> extractAndAnalyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("loanApplicationId") Long loanApplicationId)
            throws IOException {
        return bankStatementVerificationService.extractAndAnalyze(
                file,
                loanApplicationId);
    }
}