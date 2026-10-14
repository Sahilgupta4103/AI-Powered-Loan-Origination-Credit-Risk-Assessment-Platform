package com.loanplatform.aiexplanationservice.controller;

import com.loanplatform.aiexplanationservice.dto.BankStatementAnalysisResponse;
import com.loanplatform.aiexplanationservice.service.BankStatementAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/bank-statements")
public class BankStatementAnalysisController {

    private final BankStatementAnalysisService analysisService;

    public BankStatementAnalysisController(
            BankStatementAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public BankStatementAnalysisResponse analyzeStatement(
            @RequestBody BankStatementAnalysisRequest request) {

        return analysisService.analyzeStatement(request.extractedText());
    }

    public record BankStatementAnalysisRequest(String extractedText) {
    }
}