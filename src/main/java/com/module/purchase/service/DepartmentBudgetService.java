package com.module.purchase.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entityDTO.DepartmentBudgetDTO;
import com.module.purchase.mapper.DepartmentBudgetMapper;
import com.module.purchase.repository.DepartmentBudgetRepository;
import com.module.purchase.specification.DepartmentBudgetSpecification;
import java.util.List;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;

@Service
public class DepartmentBudgetService {

    @Autowired
    private DepartmentBudgetRepository departmentBudgetRepository;

    @Autowired
    private DepartmentBudgetMapper departmentBudgetMapper;

    @Autowired
    private AuditLogsService auditLogsService;

    public DepartmentBudget saveDepartmentBudget(DepartmentBudget departmentBudget) {
        return departmentBudgetRepository.save(departmentBudget);
    }

    public DepartmentBudget addDepartmentBudget(DepartmentBudget departmentBudget,Employee employee) {
        Optional<DepartmentBudget> existingDepartmentBudget = departmentBudgetRepository
                .findByDepartmentAndYear(departmentBudget.getDepartment(), departmentBudget.getYear());
        if (existingDepartmentBudget.isPresent()) {
            throw new ResourceAlreadyUsedException("Department budget for the given department and year already exists.");
        }
        departmentBudget=saveDepartmentBudget(departmentBudget);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.DEPARTMENT_BUDGET);
        log.setEntityId(departmentBudget.getDepartmentBudgetId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return departmentBudget;
    }

    public Optional<DepartmentBudget> getDepartmentBudgetById(Long id) {
        Optional<DepartmentBudget> existingDepartmentBudget = departmentBudgetRepository.findById(id);
        if (!existingDepartmentBudget.isPresent()) {
            throw new ResourceNotFoundException("Department budget not found with id: " + id);
        }
        return existingDepartmentBudget;
    }

    public Page<DepartmentBudgetDTO> getAllDepartmentBudgets(DepartmentBudgetDTO departmentDTO, int page, int size) {

        Specification<DepartmentBudget> spec = Specification
                .where(DepartmentBudgetSpecification.hasDeparmentBudgetId(departmentDTO.getDepartmentBudgetId()))
                .and(DepartmentBudgetSpecification.hasDeparment(departmentDTO.getDepartment()))
                .and(DepartmentBudgetSpecification.hasDeparmentBudgetYear(departmentDTO.getYear()));

        Pageable pageable = PageRequest.of(page, size);
        Page<DepartmentBudget> depatmentBudgetPage = departmentBudgetRepository.findAll(spec, pageable);
        return depatmentBudgetPage.map(departmentBudgetMapper::toDepartmentBudgetDTO);
    }

    public DepartmentBudget getByDepartmentAndYear(Department department, Year year) {
        DepartmentBudget exist = departmentBudgetRepository.findByDepartmentAndYear(department, year).get();
        return exist;
    }

    public DepartmentBudget updateDepartmentBudget(DepartmentBudget departmentBudget,Employee employee) {
       
        DepartmentBudget exist=getDepartmentBudgetById(departmentBudget.getDepartmentBudgetId()).get();
         if(exist.getTotalBudgetAmount()- exist.getRemainingBudgetAmount()!= departmentBudget.getTotalBudgetAmount() - departmentBudget.getRemainingBudgetAmount())
         {
            throw new ModificationNotAllowedException("This department budget spended amount is"+(exist.getTotalBudgetAmount()- exist.getRemainingBudgetAmount()));
         }
        departmentBudget=saveDepartmentBudget(departmentBudget);
        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.DEPARTMENT_BUDGET);
        log.setEntityId(departmentBudget.getDepartmentBudgetId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return departmentBudget;
    }

    public void deleteDepartmentBudgetById(Long departmentBudgetId,Employee employee) {

        DepartmentBudget exist=getDepartmentBudgetById(departmentBudgetId).get();
        if(exist.getRemainingBudgetAmount()!= exist.getTotalBudgetAmount())
        {
            throw new ResourceAlreadyUsedException("This department budget is already in use do cannot delete");
        }
        getDepartmentBudgetById(departmentBudgetId);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.DEPARTMENT_BUDGET);
        log.setEntityId(departmentBudgetId);
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        departmentBudgetRepository.deleteById(departmentBudgetId);
    }

    public List<DepartmentBudget> getDepartmentSpendingData(Year year) {
        return departmentBudgetRepository.findByYear(year);
    }
}
