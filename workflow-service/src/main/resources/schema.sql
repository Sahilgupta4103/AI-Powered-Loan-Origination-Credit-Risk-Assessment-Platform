CREATE TABLE IF NOT EXISTS workflow_inbox_events(
    event_id UUID PRIMARY KEY,
    loan_application_id BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    status VARCHAR(25) NOT NULL,
    process_instance_key VARCHAR(255) NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    last_error TEXT
);