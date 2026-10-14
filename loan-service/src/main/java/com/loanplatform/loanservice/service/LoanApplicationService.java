package com.loanplatform.loanservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanplatform.loanservice.dto.CreateLoanApplicationRequest;
import com.loanplatform.loanservice.dto.LoanApplicationResponse;
import com.loanplatform.loanservice.dto.UpdateLoanApplicationRequest;
import com.loanplatform.loanservice.entity.IdempotencyRecord;
import com.loanplatform.loanservice.entity.LoanApplication;
import com.loanplatform.loanservice.entity.OutboxEvent;
import com.loanplatform.loanservice.enums.LoanApplicationStatus;
import com.loanplatform.loanservice.exception.LoanApplicationNotFoundException;
import com.loanplatform.loanservice.mapper.LoanApplicationMapper;
import com.loanplatform.loanservice.repository.IdempotencyRecordRepository;
import com.loanplatform.loanservice.repository.LoanApplicationRepository;
import com.loanplatform.loanservice.repository.OutboxEventRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository repository;
    private final LoanApplicationMapper mapper;
    private final LoanApplicationStatusTransitionService statusTransitionService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public LoanApplicationService(LoanApplicationRepository repository, LoanApplicationMapper mapper, LoanApplicationStatusTransitionService statusTransitionService, IdempotencyRecordRepository idempotencyRecordRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper){
        this.repository = repository;
        this.mapper = mapper;
        this.statusTransitionService = statusTransitionService;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LoanApplicationResponse createLoanApplication(CreateLoanApplicationRequest request){
        LoanApplication loanApplication = mapper.toEntity(request);
        loanApplication.setStatus(LoanApplicationStatus.DRAFT);
        loanApplication.setCreatedAt(LocalDateTime.now());
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication savedApplication = repository.save(loanApplication);
        return mapper.toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "loanDetails", key = "#id", unless = "#result == null")
    public LoanApplicationResponse getLoanApplication(Long id){
        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        return mapper.toResponse(loanApplication);
    }
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getLoanAppliations(LoanApplicationStatus status, Pageable pageable){
        Page<LoanApplication>applications;
        if(status!=null){
            applications = repository.findByStatus(status,pageable);
        }
        else{
            applications = repository.findAll(pageable);
        }
        return applications.map(mapper::toResponse);
    }

    @Transactional
    @CacheEvict(value = "loanDetails", key = "#id")
    public LoanApplicationResponse updateLoanApplication(Long id, UpdateLoanApplicationRequest request){
        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        mapper.updateEntity(loanApplication,request);
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication updatedApplication = repository.save(loanApplication);
        return mapper.toResponse(updatedApplication);
    }


    @Transactional
    @CacheEvict(value = "loanDetails", key = "#id")
    public LoanApplicationResponse submitLoanApplication(Long id, String idempotencyKey){

        Optional<IdempotencyRecord> existingRecord=idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
        if(existingRecord.isPresent()){
            IdempotencyRecord record = existingRecord.get();
            if(!record.getLoanApplicationId().equals(id)){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency key already used for another loan");
            }
            LoanApplication existingApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
            return mapper.toResponse(existingApplication);
        }

        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        statusTransitionService.validTransition(loanApplication.getStatus(), LoanApplicationStatus.SUBMITTED);
        loanApplication.setStatus(LoanApplicationStatus.SUBMITTED);
        loanApplication.setUpdatedAt(LocalDateTime.now());

        // save data to loan DB
        LoanApplication submittedApplication = repository.save(loanApplication);
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setLoanApplicationId(id);
        record.setCreatedAt(LocalDateTime.now());

        // save key and data to idempotency DB
        idempotencyRecordRepository.save(record);

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("eventId", UUID.randomUUID().toString());
        eventPayload.put("eventType", "LoanSubmitted");
        eventPayload.put("loanApplicationId", id);
        eventPayload.put("timestamp", LocalDateTime.now().toString());
        String payload;
        try{
            payload = objectMapper.writeValueAsString(eventPayload);
        }
        catch(JsonProcessingException e){
            throw new RuntimeException("Error serializing event payload", e);
        }

        // save to outbox table for event publishing
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("LOAN_APPLICATION");
        outboxEvent.setAggregateId(id);
        outboxEvent.setEventType("LoanSubmitted");
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus("PENDING");
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setRetryCount(0);
        outboxEventRepository.save(outboxEvent);

        return mapper.toResponse(submittedApplication);
    }

    @Transactional
    @CacheEvict(value = "loanDetails", key = "#id")
    public LoanApplicationResponse updateStatus(Long id, LoanApplicationStatus requestedStatus){
        LoanApplication loanApplication = repository.findById(id).orElseThrow(() -> new LoanApplicationNotFoundException(id));
        LoanApplicationStatus currentStatus = loanApplication.getStatus();
        statusTransitionService.validTransition(currentStatus, requestedStatus);
        loanApplication.setStatus(requestedStatus);
        loanApplication.setUpdatedAt(LocalDateTime.now());
        LoanApplication updatedLoanApplication = repository.save(loanApplication);
        return mapper.toResponse(updatedLoanApplication);
    }

}
