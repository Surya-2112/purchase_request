package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Unit;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.repository.UnitRepository;
import com.module.purchase.specification.UnitSpecification;

@Service
@Transactional
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    // SAVE BASE METHOD
    public Unit saveUnit(Unit unit) {
        return unitRepository.save(unit);
    }

    // CREATE
    public Unit addUnit(Unit unit, Employee employee) {

        Optional<Unit> existing = unitRepository.findByName(unit.getName());

        if (existing.isPresent()) {
            throw new ResourceAlreadyUsedException(
                    "Unit already exists with name: " + unit.getName());
        }

        unit = saveUnit(unit);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.UNIT);
        log.setEntityId(Long.valueOf(unit.getId()));
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

       // auditLogsService.addAuditLog(log);

        return unit;
    }

    // GET BY ID
    public Optional<Unit> getUnitById(Integer id) {

        Optional<Unit> unit = unitRepository.findById(id);

        if (unit.isEmpty()) {
            throw new ResourceNotFoundException("Unit not found with id: " + id);
        }

        return unit;
    }

    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }

    // PAGINATION + FILTER
    public Page<Unit> getUnits(Unit unit, Pageable pageable) {

        Specification<Unit> spec = Specification
                .where(UnitSpecification.hasId(unit.getId()))
                .and(UnitSpecification.hasName(unit.getName()))
                .and(UnitSpecification.hasCode(unit.getCode()));

        return unitRepository.findAll(spec, pageable);
    }

    // UPDATE
    public Unit updateUnit(Unit unit, Employee employee) {

        getUnitById(unit.getId()).get();

        unit = saveUnit(unit);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.UNIT);
        log.setEntityId(Long.valueOf(unit.getId()));
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

       // auditLogsService.addAuditLog(log);

        return unit;
    }

    // DELETE
    public void deleteUnitById(Integer id, Employee employee) {

        Unit existing = getUnitById(id).get();

        if (existing.getItems() != null && !existing.getItems().isEmpty()) {
            throw new ResourceAlreadyUsedException(
                    "Cannot delete unit because it is used in items");
        }

        unitRepository.deleteById(id);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.UNIT);
        log.setEntityId(Long.valueOf(id));
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

       // auditLogsService.addAuditLog(log);
    }
}