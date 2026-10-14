package com.loanplatform.workflowservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class WorkflowInboxRepository {

    private final JdbcTemplate jdbcTemplate;
    public WorkflowInboxRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    public boolean claimEvent(UUID eventId, Long loanApplicationId, String eventType){
        int inserted = jdbcTemplate.update("""
                INSERT INTO workflow_inbox_events(event_id, loan_application_id, event_type, status)
                VALUES(?, ?, ?, 'RECEIVED') ON CONFLICT (event_id) DO NOTHING""",eventId, loanApplicationId, eventType);
        if(inserted == 1){
            return true;
        }
        int retried = jdbcTemplate.update("""
                UPDATE workflow_inbox_events
                SET status = 'RECEIVED'
                WHERE event_id = ? AND status = 'FAILED'""",eventId);
        return retried==1;
    }

    public void markProcessed(UUID eventId, String processInstanceKey){
        jdbcTemplate.update("""
                UPDATE workflow_inbox_events
                SET status = 'PROCESSED',
                    process_instance_key = ?,
                    processed_at = CURRENT_TIMESTAMP
                WHERE event_id = ?""",processInstanceKey, eventId);
    }

    public void markFailed(UUID eventId, String errorMessage) {
        jdbcTemplate.update("""
            UPDATE workflow_inbox_events
            SET status = 'FAILED',
                last_error = ?
            WHERE event_id = ?
            """,
                errorMessage,
                eventId
        );
    }
}
