package com.loanplatform.aiexplanationservice.dto;

public record ManagerReviewSummaryResponse(
    Long loanApplicationId,
    String summary
) {
}
