
package com.loanplatform.workflowservice.messaging;

import com.loanplatform.workflowservice.repository.WorkflowInboxRepository;
import com.loanplatform.workflowservice.service.WorkflowService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
public class LoanSubmittedEventConsumer {

    private final JsonMapper jsonMapper;
    private final WorkflowService workflowService;
    private final WorkflowInboxRepository workflowInboxRepository;

    public LoanSubmittedEventConsumer(
            JsonMapper jsonMapper,
            WorkflowService workflowService,
            WorkflowInboxRepository workflowInboxRepository){
        this.jsonMapper = jsonMapper;
        this.workflowService = workflowService;
        this.workflowInboxRepository = workflowInboxRepository;
    }

    @KafkaListener(
            topics = "loan-events",
            groupId = "loan-workflow-consumer-v3"
    )
    public void consume(String message) {

        JsonNode event = jsonMapper.readTree(message);
        JsonNode loanIdNode = event.get("loanApplicationId");
        JsonNode eventIdNode = event.get("eventId");
        JsonNode eventTypeNode = event.get("eventType");

        if (loanIdNode == null || !loanIdNode.canConvertToLong() || eventIdNode == null || eventIdNode.isNull() || eventTypeNode == null || eventTypeNode.isNull()) {
            throw new IllegalArgumentException(
                    "Missing or invalid loanApplicationId in event: " + message
            );
        }

        String eventType = eventTypeNode.asString();
        Long loanApplicationId = loanIdNode.asLong();

        if(!"LoanSubmitted".equals(eventType)){
            System.out.println("SKIPPING UNSUPPORTED EVENT TYPE " + eventType);
            return;
        }

        UUID eventId = UUID.fromString(eventIdNode.asString());
        boolean claimed = workflowInboxRepository.claimEvent(eventId, loanApplicationId, eventType);

        if (!claimed) {
            System.out.println("Event already claimed: " + eventId);
            return;
        }

        try {
            String processInstanceKey = workflowService.StartLoanOriginationWorkflow(loanApplicationId);

            workflowInboxRepository.markProcessed(eventId, processInstanceKey);


            System.out.println(
                    "Started loan-origination workflow for loanApplicationId="
                            + loanApplicationId
                            + ", processInstanceKey="
                            + processInstanceKey
            );
        }catch (RuntimeException e) {
        e.printStackTrace(); // Add this to print the real underlying error
        try {
            workflowInboxRepository.markFailed(eventId, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        } catch (RuntimeException inboxException) {
            e.addSuppressed(inboxException);
        }
        throw e;
    }
    }
}
