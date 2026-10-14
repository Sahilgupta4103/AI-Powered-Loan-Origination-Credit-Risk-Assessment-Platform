package com.loanplatform.aiexplanationservice.controller;

import com.loanplatform.aiexplanationservice.dto.ManagerReviewSummaryResponse;
import com.loanplatform.aiexplanationservice.service.ManagerReviewSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/ai")
public class ManagerReviewSummaryController {

    private final ManagerReviewSummaryService summaryService;
    public ManagerReviewSummaryController(ManagerReviewSummaryService summaryService){
        this.summaryService=summaryService;
    }

    @GetMapping("/loans/{loanApplicationId}/manager-summary")
    public ResponseEntity<ManagerReviewSummaryResponse> generateSummary(@PathVariable Long loanApplicationId){
        return ResponseEntity.ok(summaryService.generateSummary(loanApplicationId));
    }

}
