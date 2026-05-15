package com.module.purchase.service;

import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entityDTO.EmployeeDTO;
import com.module.purchase.mapper.EmployeeMapper;
import com.module.purchase.repository.EmployeeRepository;
import com.module.purchase.specification.EmployeeSpecification;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    @Lazy
    private DepartmentService departmentService;

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee addEmployee(Employee employee) {
        Optional<Employee> existingEmployee = employeeRepository.findByEmployeeEmail(employee.getEmployeeEmail());
        if (existingEmployee.isPresent()) {
            throw new ResourceAlreadyUsedException(
                    "Employee with email " + employee.getEmployeeEmail() + " already exists");
        }
        employee.setActive(true);
        return saveEmployee(employee);
    }

    public Optional<Employee> getEmployeeById(Long id) {

        Optional<Employee> existingEmployee = employeeRepository.findById(id);
        if (!existingEmployee.isPresent()) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        return existingEmployee;
    }

    public Page<EmployeeDTO> getAllEmployees(EmployeeDTO employeeDTO, int page, int size) {

        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasEmployeeId(employeeDTO.getEmployeeId()))
                .and(EmployeeSpecification.hasEmployeeName(employeeDTO.getEmployeeName()))
                .and(EmployeeSpecification.hasDepartment(employeeDTO.getDepartment()))
                .and(EmployeeSpecification.hasRole(employeeDTO.getRole()))
                .and(EmployeeSpecification.hasActive(employeeDTO.getActive()));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        return employeePage.map(employeeMapper::toEmployee);
    }

    public List<Employee> getEmployees()
    {
        return employeeRepository.findAll();
    }

    public Employee updateEmployee(Employee employee) {
        Employee existingEmployee = getEmployeeById(employee.getEmployeeId()).get();
        if (!existingEmployee.getEmployeeEmail().equals(employee.getEmployeeEmail())) {
            throw new ModificationNotAllowedException("cannot update employee email");
        }
         if( existingEmployee.getDepartment()!=null && employee.getDepartment()!=existingEmployee.getDepartment())
        {
            Department department=departmentService.getDepartmentById(existingEmployee.getDepartment().getDepartmentId()).get();
            if(department.getHeadEmployee()==existingEmployee)
            {
                throw new RuntimeException("This employee is  head of department");
            }
        }
        return saveEmployee(employee);
    }

    public void deleteEmployeeById(Long employeeId) {
        Employee existingEmployee = getEmployeeById(employeeId).get();
        if (existingEmployee.getAssignedApprovals() != null && !existingEmployee.getAssignedApprovals().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated assingned approvals");
        }
        if (existingEmployee.getAuditLogs() != null && !existingEmployee.getAuditLogs().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated audit logs");
        }
        if (existingEmployee.getForApprovals() != null && !existingEmployee.getForApprovals().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated requested approvals");
        }
        if (existingEmployee.getPurchaseRequestHeaders() != null
                && !existingEmployee.getPurchaseRequestHeaders().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated purchase request header");
        }
        if (existingEmployee.getPurchaseOrderHeaders() != null
                && !existingEmployee.getPurchaseOrderHeaders().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated purchase order header");
        }
        employeeRepository.deleteById(employeeId);
    }
}
