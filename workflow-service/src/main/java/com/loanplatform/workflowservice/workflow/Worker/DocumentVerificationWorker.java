package com.loanplatform.workflowservice.workflow.Worker;

import com.loanplatform.workflowservice.client.LoanServiceClient;
import com.loanplatform.workflowservice.service.BankStatementPdfExtractor;
import com.loanplatform.workflowservice.service.BankStatementVerificationService;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class DocumentVerificationWorker {

    private static final String DEFAULT_BANK_STATEMENT_PATH =
            "/Users/sahilgupta/Downloads/Bank_Statement.pdf";

    private final LoanServiceClient loanServiceClient;
    private final BankStatementVerificationService bankStatementVerificationService;
    private final BankStatementPdfExtractor bankStatementPdfExtractor;

    public DocumentVerificationWorker(
            LoanServiceClient loanServiceClient,
            BankStatementVerificationService bankStatementVerificationService,
            BankStatementPdfExtractor bankStatementPdfExtractor) {
        this.loanServiceClient = loanServiceClient;
        this.bankStatementVerificationService = bankStatementVerificationService;
        this.bankStatementPdfExtractor = bankStatementPdfExtractor;
    }

    @JobWorker(type = "document-verification")
    public void verifyDocument(JobClient client, ActivatedJob job) {
        var variables = job.getVariablesAsMap();

        Long loanApplicationId = variables.get("loanApplicationId") instanceof Number value
                ? value.longValue()
                : null;

        if (loanApplicationId == null || loanApplicationId <= 0) {
            throw new IllegalArgumentException(
                    "loanApplicationId is missing or wrong in the workflow variables");
        }

        String bankStatementText = variables.get("bankStatementText") instanceof String text
                ? text
                : null;

        if (bankStatementText == null || bankStatementText.isBlank()) {
            bankStatementText = extractTextFromDefaultDocument();
        }

        System.out.println("Document Verification Started for: " + loanApplicationId);
        loanServiceClient.updateLoanStatus(loanApplicationId, "DOCUMENT_VERIFICATION");

        var result = bankStatementVerificationService.analyzeExtractedText(
                loanApplicationId,
                bankStatementText,
                "workflow-bank-statement.pdf");

        boolean loanEligible = Boolean.TRUE.equals(result.get("loanEligible"));
        String decision = loanEligible ? "APPROVE" : "REJECT";

        if (loanEligible) {
            var currentApplication = loanServiceClient.getLoanApplication(loanApplicationId);
            String currentStatus = currentApplication == null ? "" : currentApplication.status();
            if (!"CREDIT_ASSESSMENT".equalsIgnoreCase(currentStatus)) {
                loanServiceClient.updateLoanStatus(loanApplicationId, "CREDIT_ASSESSMENT");
            }

            System.out.println("Document Verified for: " + loanApplicationId);
            client.newCompleteCommand(job)
                    .variables(Map.of(
                            "documentsVerified", true,
                            "decision", decision,
                            "bankStatementDecision", result.get("affordabilityDecision"),
                            "affordabilityStatus", result.get("affordabilityStatus")))
                    .send()
                    .join();
            System.out.println("Document Verification Completed");
        } else {
            System.out.println("Document Verification Failed for: " + loanApplicationId);
            client.newCompleteCommand(job)
                    .variables(Map.of(
                            "documentsVerified", false,
                            "decision", decision,
                            "bankStatementDecision", result.get("affordabilityDecision"),
                            "affordabilityStatus", result.get("affordabilityStatus")))
                    .send()
                    .join();
        }
    }

    private String extractTextFromDefaultDocument() {
        Path path = Paths.get(DEFAULT_BANK_STATEMENT_PATH);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "Default bank statement PDF not found at: " + DEFAULT_BANK_STATEMENT_PATH);
        }

        try {
            byte[] bytes = Files.readAllBytes(path);
            MultipartFile file = new FixedMultipartFile(
                    "file",
                    path.getFileName().toString(),
                    "application/pdf",
                    bytes);
            return bankStatementPdfExtractor.extractText(file);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read default bank statement file: " + DEFAULT_BANK_STATEMENT_PATH,
                    e);
        }
    }

    private static final class FixedMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        private FixedMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public java.io.InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), content);
        }

        @Override
        public void transferTo(java.nio.file.Path dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest, content);
        }
    }
}

