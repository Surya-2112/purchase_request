package com.module.purchase.service;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.Employee;
import com.module.purchase.entityDTO.DepartmentBudgetDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.mapper.DepartmentBudgetMapper;
import com.module.purchase.repository.DepartmentBudgetRepository;
import com.module.purchase.specification.DepartmentBudgetSpecification;

@Service
@Transactional
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
        Optional<DepartmentBudget> existingDepartmentBudget = departmentBudgetRepository.findByDepartmentAndYear(departmentBudget.getDepartment(), departmentBudget.getYear());
        if (existingDepartmentBudget.isPresent()) {
            throw new ResourceAlreadyUsedException("Department budget for the given department and year already exists.");
        }
        if(departmentBudget.getRemainingBudgetAmount()>departmentBudget.getTotalBudgetAmount())
        {
            throw new RuntimeException("Department Budget not valid");
        }
        departmentBudget=saveDepartmentBudget(departmentBudget);
        auditLogsService.addAuditLog(EntityType.DEPARTMENT_BUDGET,departmentBudget.getDepartmentBudgetId(),Action.CREATE,employee);
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
        Optional<DepartmentBudget> exist = departmentBudgetRepository.findByDepartmentAndYear(department, year);
        return exist.orElse(null);
    }

    public DepartmentBudget updateDepartmentBudget(DepartmentBudget departmentBudget,Employee employee) {
       
        DepartmentBudget exist=getDepartmentBudgetById(departmentBudget.getDepartmentBudgetId()).get();
         if(exist.getTotalBudgetAmount()-(exist.getRemainingBudgetAmount()) != departmentBudget.getTotalBudgetAmount()-(departmentBudget.getRemainingBudgetAmount()))
         {
            throw new ModificationNotAllowedException("This department budget spended amount is"+(exist.getTotalBudgetAmount()-(exist.getRemainingBudgetAmount())));
         }
        departmentBudget=saveDepartmentBudget(departmentBudget);
        auditLogsService.addAuditLog(EntityType.DEPARTMENT_BUDGET,departmentBudget.getDepartmentBudgetId(),Action.UPDATE,employee);
        return departmentBudget;
    }

    public void deleteDepartmentBudgetById(Long departmentBudgetId,Employee employee) {

        DepartmentBudget exist=getDepartmentBudgetById(departmentBudgetId).get();
        if(!exist.getRemainingBudgetAmount().equals(exist.getTotalBudgetAmount()))
        {
            throw new ResourceAlreadyUsedException("This department budget is already in use do cannot delete");
        }
        getDepartmentBudgetById(departmentBudgetId);

        auditLogsService.addAuditLog(EntityType.DEPARTMENT_BUDGET,departmentBudgetId,Action.DELETE,employee);

        departmentBudgetRepository.deleteById(departmentBudgetId);
    }

    public List<DepartmentBudget> getDepartmentSpendingData(Year year) {
        return departmentBudgetRepository.findByYear(year);
    }

    public List<DepartmentBudget> getDepartmentBudgetByDepartment(Department department)
    {
        return departmentBudgetRepository.findByDepartment(department);
    }
}
