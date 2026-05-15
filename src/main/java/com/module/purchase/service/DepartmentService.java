package com.module.purchase.service;

import org.springframework.stereotype.Service;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.repository.DepartmentRepository;
import com.module.purchase.specification.DepartmentSpecification;
import com.module.purchase.entity.Department;
import com.module.purchase.entityDTO.DepartmentDTO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceMissingFieldException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.mapper.DepartmentMapper;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    @Lazy
    private EmployeeService employeeSerivce;

    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public Department addDepartment(Department department) {
       
        Optional<Department> existingDepartment = departmentRepository.findByDepartmentCode(department.getDepartmentCode());
        if (existingDepartment.isPresent()) {
            throw new ResourceAlreadyUsedException("Department with code " + department.getDepartmentCode() + " already exists.");
        }
        return saveDepartment(department);
    }

    public Optional<Department> getDepartmentById(Long id) {
        if(id==null)
        {
            throw new ResourceMissingFieldException("Department Id is needed");
        }
        Optional<Department> existingDepartment = departmentRepository.findById(id);
        if(!existingDepartment.isPresent()) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        return existingDepartment;
    }

    public Page<DepartmentDTO> getAllDepartments(DepartmentDTO departmentDTO,int page , int size) {

        Specification<Department> spec= Specification
        .where(DepartmentSpecification.hasDepartmentId(departmentDTO.getDepartmentId()))
        .and(DepartmentSpecification.hasDepartmentName(departmentDTO.getDepartmentName()))
        .and(DepartmentSpecification.hasDepartmentCode(departmentDTO.getDepartmentCode()))
        .and(DepartmentSpecification.hasActive(departmentDTO.getActive()));

        Pageable pageable= PageRequest.of(page,size);
        Page<Department> departmentPage = departmentRepository.findAll(spec,pageable);
        return departmentPage.map(departmentMapper::toDepartmentDTO);
    }

    public List<Department> getDepartments()
    {
        return departmentRepository.findAll();
    }

    public Department updateDepartment(Department department)
    {
        Department existingDepartment=getDepartmentById(department.getDepartmentId()).get();
        if(!existingDepartment.getDepartmentCode().equals(department.getDepartmentCode()))
        {
            throw new ModificationNotAllowedException("cannot update department code ");
        }
        if(department.getHeadEmployee()!=null && existingDepartment.getHeadEmployee() != department.getHeadEmployee())
        {  long employeeId=department.getHeadEmployee().getEmployeeId();
            if(employeeSerivce.getEmployeeById(employeeId).get().getDepartment()!=existingDepartment)
            {
                throw new RuntimeException("Employee is not in this department");
            }
        }
        return saveDepartment(department);
    }

    public void deleteDepartmentById(Long departmentId)
    {
        Department existingDepartment=getDepartmentById(departmentId).get();

        if(existingDepartment.getDepartmentBudgets()!=null && !existingDepartment.getDepartmentBudgets().isEmpty())
        {
            throw new ResourceAlreadyUsedException("Cannot delete department with associated department budgets");
        }
        if(existingDepartment.getEmployees()!=null && !existingDepartment.getEmployees().isEmpty())
        {
            throw new ResourceAlreadyUsedException("Cannot delete department with associated employee");
        }
        if(existingDepartment.getPurchaseRequestHeaders()!=null && !existingDepartment.getPurchaseRequestHeaders().isEmpty())
        {
            throw new ResourceAlreadyUsedException("Cannot delete department with associated purchase request headers");
        }
        departmentRepository.deleteById(departmentId);
    }
}
