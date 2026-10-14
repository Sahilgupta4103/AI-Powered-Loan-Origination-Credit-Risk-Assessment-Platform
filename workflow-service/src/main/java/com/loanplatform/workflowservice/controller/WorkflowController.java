package com.loanplatform.workflowservice.controller;

import com.loanplatform.workflowservice.service.WorkflowService;
import com.loanplatform.workflowservice.dto.StartLoanWorkflowRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService){
        this.workflowService = workflowService;
    }

    @PostMapping("/loan-origination")
    public ResponseEntity<Map<String,String>> startLoanOrigination(@RequestBody StartLoanWorkflowRequest request){
        String processInstanceKey = workflowService.StartLoanOriginationWorkflow(request.getLoanApplicationId());
        return ResponseEntity.ok(Map.of("message", "Loan workflow started successfully", "processInstanceKey", processInstanceKey));
    }

}
