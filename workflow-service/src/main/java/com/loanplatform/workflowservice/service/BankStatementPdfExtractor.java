
package com.loanplatform.workflowservice.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class BankStatementPdfExtractor {

    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bank statement PDF is required");
        }

        if (file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        try (PDDocument document =
                     Loader.loadPDF(file.getBytes())) {

            if (document.isEncrypted()) {
                throw new IllegalArgumentException(
                        "Password-protected PDFs are not supported yet");
            }

            String extractedText =
                    new PDFTextStripper().getText(document);

            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text found. The PDF may be scanned or empty.");
            }

            return extractedText.trim();
        }
    }
}
