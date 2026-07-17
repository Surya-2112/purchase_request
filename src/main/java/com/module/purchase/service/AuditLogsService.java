package com.module.purchase.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.repository.AuditLogsRepository;
import com.module.purchase.specification.AuditLogsSpecification;

@Service
@Transactional
public class AuditLogsService {
    
    @Autowired
    private AuditLogsRepository auditLogsRepository;

    public AuditLogs saveAuditLog(AuditLogs auditLog) {
        return auditLogsRepository.save(auditLog);
    }

    public AuditLogs addAuditLog(EntityType entityType,Long id,Action action,Employee doneBy) {
        AuditLogs log= new AuditLogs();
        log.setEntityType(entityType);
        log.setEntityId(id);
        log.setAction(action);
        log.setPerformedBy(doneBy);
        log.setTimestamp(OffsetDateTime.now());
        return saveAuditLog(log);
    }
    
    public Optional<AuditLogs> getAuditLogById(Long id) {
       Optional<AuditLogs> existingAuditLog = auditLogsRepository.findById(id);
        if (!existingAuditLog.isPresent()) {
                throw new ResourceNotFoundException("Audit log not found with id: " + id);
        }
       return existingAuditLog;
    }

    public Page<AuditLogs> getAuditLogsHasPage(AuditLogs auditLogs,LocalDate date,int page , int size) {

        Specification<AuditLogs> spec=Specification.
        where(AuditLogsSpecification.hasAuditLogId(auditLogs.getAuditLogId()))
        .and(AuditLogsSpecification.hasEntityType(auditLogs.getEntityType()))
        .and(AuditLogsSpecification.hasEntityId(auditLogs.getEntityId()))
        .and(AuditLogsSpecification.hasAction(auditLogs.getAction()))
        .and(AuditLogsSpecification.hasPerformedBy(auditLogs.getPerformedBy()))
        .and(AuditLogsSpecification.hasTimestamp(date));

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogs> categoryPage = auditLogsRepository.findAll(spec, pageable);
        return categoryPage;
    }

    public List<AuditLogs> getAuditLogsList(AuditLogs auditLogs,LocalDate date) {

        Specification<AuditLogs> spec=Specification.
        where(AuditLogsSpecification.hasAuditLogId(auditLogs.getAuditLogId()))
        .and(AuditLogsSpecification.hasEntityType(auditLogs.getEntityType()))
        .and(AuditLogsSpecification.hasEntityId(auditLogs.getEntityId()))
        .and(AuditLogsSpecification.hasAction(auditLogs.getAction()))
        .and(AuditLogsSpecification.hasPerformedBy(auditLogs.getPerformedBy()))
        .and(AuditLogsSpecification.hasTimestamp(date));

        return  auditLogsRepository.findAll(spec);
    }

    public Long getAuditLogsCount(AuditLogs auditLogs,LocalDate date) {

        Specification<AuditLogs> spec=Specification.
        where(AuditLogsSpecification.hasAuditLogId(auditLogs.getAuditLogId()))
        .and(AuditLogsSpecification.hasEntityType(auditLogs.getEntityType()))
        .and(AuditLogsSpecification.hasEntityId(auditLogs.getEntityId()))
        .and(AuditLogsSpecification.hasAction(auditLogs.getAction()))
        .and(AuditLogsSpecification.hasPerformedBy(auditLogs.getPerformedBy()))
        .and(AuditLogsSpecification.hasTimestamp(date));
        return  auditLogsRepository.count(spec);
    }
}
