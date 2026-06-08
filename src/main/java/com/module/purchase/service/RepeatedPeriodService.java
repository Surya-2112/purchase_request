package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.repository.RepeatedPeriodRepository;

@Service
@Transactional
public class RepeatedPeriodService {

    @Autowired
    private RepeatedPeriodRepository repeatedPeriodRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    public RepeatedPeriod save(RepeatedPeriod repeatedPeriod) {
        return repeatedPeriodRepository.save(repeatedPeriod);
    }

    // FIND BY POLYMORPHIC PAIR
    public Optional<RepeatedPeriod> findByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId) {
        return repeatedPeriodRepository.findByReferTypeAndReferId(referType, referId);
    }

    // FETCH BY TARGET EXECUTION TIMELINE
    public List<RepeatedPeriod> findAllByNextDate(LocalDate nextDate) {
        return repeatedPeriodRepository.findAllByNextDate(nextDate);
    }

   
    public RepeatedPeriod addRepeatedPeriod(RepeatedPeriod repeatedPeriod, Employee employee) {
        // Automatically default next execution mark to matching start baseline if missing
        if (repeatedPeriod.getNextDate() == null) {
            repeatedPeriod.setNextDate(repeatedPeriod.getFromDate());
        }

        repeatedPeriod = save(repeatedPeriod);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REPEATED_PERIOD); // Ensure REPEATED_PERIOD is added to EntityType Enum
        log.setEntityId(repeatedPeriod.getId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        // auditLogsService.addAuditLog(log);

        return repeatedPeriod;
    }

    // GET BY ID
    public Optional<RepeatedPeriod> getRepeatedPeriodById(Long id) {
        Optional<RepeatedPeriod> period = repeatedPeriodRepository.findById(id);

        if (period.isEmpty()) {
            throw new ResourceNotFoundException("Repeated period schedule not found with id: " + id);
        }

        return period;
    }

    // UPDATE WITH AUDIT LOGGING
    public RepeatedPeriod updateRepeatedPeriod(RepeatedPeriod repeatedPeriod, Employee employee) {
        getRepeatedPeriodById(repeatedPeriod.getId()); // Throws exception if it doesn't exist

        repeatedPeriod = save(repeatedPeriod);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REPEATED_PERIOD);
        log.setEntityId(repeatedPeriod.getId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        // auditLogsService.addAuditLog(log);

        return repeatedPeriod;
    }

    // DELETE BY ID
    public void deleteRepeatedPeriodById(Long id, Employee employee) {
        getRepeatedPeriodById(id); // Verifies existence first

        repeatedPeriodRepository.deleteById(id);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REPEATED_PERIOD);
        log.setEntityId(id);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        // auditLogsService.addAuditLog(log);
    }

    // ORPHAN REMOVAL (Used when unchecking Auto-RFQ checkboxes)
    public void deleteByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId) {
        repeatedPeriodRepository.deleteByReferTypeAndReferId(referType, referId);
    }
}