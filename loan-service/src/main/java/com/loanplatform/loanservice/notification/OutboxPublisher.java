package com.loanplatform.loanservice.notification;

import com.loanplatform.loanservice.entity.OutboxEvent;
import com.loanplatform.loanservice.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final String TOPIC = "loan-events";
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private static final int MAX_RETRIES = 5;
    public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String,String> kafkaTemplate){
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents(){
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        for(OutboxEvent event: events){
            try{
                log.info("Publishing outbox events: " + event.getId() + " with payload: " + event.getPayload());
                kafkaTemplate.send(TOPIC,String.valueOf(event.getAggregateId()),event.getPayload()).get();
                event.setStatus("PUBLISHED");
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                log.info("Outbox event published successfully: " + event.getId());
            }
            catch (Exception e){
                event.setRetryCount(event.getRetryCount() + 1);
                if(event.getRetryCount() >= MAX_RETRIES){
                    event.setStatus("FAILED");
                    log.error("Outbox event: " + event.getId() + " has failed after maximum retries.");
                }
                outboxEventRepository.save(event);
                log.error("Failed to publish outbox event: " + event.getId() + " due to: " + e.getMessage() + ". Retry count: " + event.getRetryCount());
            }
        }
    }

}
