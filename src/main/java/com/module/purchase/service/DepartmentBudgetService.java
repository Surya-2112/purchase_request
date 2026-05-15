package com.module.purchase.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entityDTO.DepartmentBudgetDTO;
import com.module.purchase.mapper.DepartmentBudgetMapper;
import com.module.purchase.repository.DepartmentBudgetRepository;
import com.module.purchase.specification.DepartmentBudgetSpecification;

@Service
public class DepartmentBudgetService {

    @Autowired
    private DepartmentBudgetRepository departmentBudgetRepository;

    @Autowired
    private DepartmentBudgetMapper departmentBudgetMapper;

    public DepartmentBudget saveDepartmentBudget(DepartmentBudget departmentBudget) {
        return departmentBudgetRepository.save(departmentBudget);
    }

    public DepartmentBudget addDepartmentBudget(DepartmentBudget departmentBudget) {
        Optional<DepartmentBudget> existingDepartmentBudget = departmentBudgetRepository.findByDepartmentAndYear(departmentBudget.getDepartment(), departmentBudget.getYear());
        if (existingDepartmentBudget.isPresent()) {
            throw new RuntimeException("Department budget for the given department and year already exists.");
        }
        return saveDepartmentBudget(departmentBudget);
    }

    public Optional<DepartmentBudget> getDepartmentBudgetById(Long id) {
       Optional<DepartmentBudget> existingDepartmentBudget = departmentBudgetRepository.findById(id); 
        if (!existingDepartmentBudget.isPresent()) {
                throw new RuntimeException("Department budget not found with id: " + id);
        }
       return existingDepartmentBudget;
    }

    public Page<DepartmentBudgetDTO> getAllDepartmentBudgets(DepartmentBudgetDTO departmentDTO,int page,int size) {

        Specification<DepartmentBudget> spec= Specification
        .where(DepartmentBudgetSpecification.hasDeparmentBudgetId(departmentDTO.getDepartmentBudgetId()))
        .and(DepartmentBudgetSpecification.hasDeparment(departmentDTO.getDepartment()))
        .and(DepartmentBudgetSpecification.hasDeparmentBudgetYear(departmentDTO.getYear()));

        Pageable pageable= PageRequest.of(page, size);
        Page<DepartmentBudget> depatmentBudgetPage= departmentBudgetRepository.findAll(spec,pageable);
        return depatmentBudgetPage.map(departmentBudgetMapper::toDepartmentBudgetDTO);
    }

    public DepartmentBudget updateDepartmentBudget(DepartmentBudget departmentBudget)
    {   //TODO : need add 
         return saveDepartmentBudget(departmentBudget);
    }

    public void deleteDepartmentBudgetById(Long departmentBudgetId)
    {   getDepartmentBudgetById(departmentBudgetId);
       // if(existingDepartmentBudget.getP)   //TODO : need to write 
        departmentBudgetRepository.deleteById(departmentBudgetId);
    }
}
