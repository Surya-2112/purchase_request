package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.repository.AuditLogsRepository;
import java.util.Optional;
import java.util.List;


@Service
public class AuditLogsService {
    
    @Autowired
    private AuditLogsRepository auditLogsRepository;

    public AuditLogs saveAuditLog(AuditLogs auditLog) {
        return auditLogsRepository.save(auditLog);
    }

    public AuditLogs addAuditLog(AuditLogs auditLog) {
        return saveAuditLog(auditLog);
    }
    
    public Optional<AuditLogs> getAuditLogById(Long id) {
       Optional<AuditLogs> existingAuditLog = auditLogsRepository.findById(id);
        if (!existingAuditLog.isPresent()) {
                throw new RuntimeException("Audit log not found with id: " + id);
        }
       return existingAuditLog;
    }

    public List<AuditLogs> getAllAuditLogs() {
        return auditLogsRepository.findAll();
    }
}
