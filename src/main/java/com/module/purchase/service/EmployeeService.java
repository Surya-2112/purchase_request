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
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.entityDTO.EmployeeDTO;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.EntityType;
import com.module.purchase.mapper.EmployeeMapper;
import com.module.purchase.repository.EmployeeRepository;
import com.module.purchase.specification.EmployeeSpecification;

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

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    @Lazy
    private PurchaseRequestHeaderService purchaseRequestHeaderService;

    @Autowired
    private PurchaseOrderHeaderService purchaseOrderHeaderService;

    @Autowired
    private AssigningApprovalsService assigningApprovalService;

    @Autowired
    @Lazy
    private UsersService userService;

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee addEmployee(Employee employee, Employee created) {
        Optional<Employee> existingEmployee = employeeRepository.findByEmployeeEmail(employee.getEmployeeEmail());
        if (existingEmployee.isPresent()) {
            throw new ResourceAlreadyUsedException(
                    "Employee with email " + employee.getEmployeeEmail() + " already exists");
        }

        employee.setActive(true);
        employee = saveEmployee(employee);

        auditLogsService.addAuditLog(EntityType.EMPLOYEE,employee.getEmployeeId(),Action.CREATE,employee);

        return employee;
    }

    public List<Employee> getEmployeesByEmployeeGroup(EmployeeGroup employeeGroup) {
        return employeeRepository.findByRoleEmployeeGroup(employeeGroup);
    }

    public Optional<Employee> getEmployeeById(Long id) {

        Optional<Employee> existingEmployee = employeeRepository.findById(id);
        if (!existingEmployee.isPresent()) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
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

    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> getEmplyeesWithoutUsers() {
        return employeeRepository.findEmployeesWithoutUser();
    }

    public Employee updateEmployee(Employee employee, Employee updated) {

        Employee existingEmployee = getEmployeeById(employee.getEmployeeId()).get();
        if (!existingEmployee.getEmployeeEmail().equals(employee.getEmployeeEmail())) {
            throw new ModificationNotAllowedException("cannot update employee email");
        }
        if (existingEmployee.getDepartment() != null && employee.getDepartment() != existingEmployee.getDepartment()) {
            Department department = departmentService
                    .getDepartmentById(existingEmployee.getDepartment().getDepartmentId()).get();
            if (department.getHeadEmployee() == existingEmployee) {
                throw new RuntimeException("This employee is  head of department");
            }
        }
        auditLogsService.addAuditLog(EntityType.EMPLOYEE,employee.getEmployeeId(),Action.UPDATE,employee);

        if(employee.getActive()==false && employee.getUsers()!=null)
        {   
            employee.getUsers().setActive(false);
            userService.updateUser(employee.getUsers(),updated);
        }
        employee = saveEmployee(employee);        
        return employee;
    }

    public void deleteEmployeeById(Long employeeId, Employee deleted) {
        Employee existingEmployee = getEmployeeById(employeeId).get();

        AuditLogs auditLogs = new AuditLogs();
        auditLogs.setPerformedBy(existingEmployee);
        Long auditLogsCount = auditLogsService.getAuditLogsCount(auditLogs, null);

        AssigningApprovalsDTO approvals = new AssigningApprovalsDTO();
        approvals.setAssignedBy(existingEmployee);
        Long approvalCount = assigningApprovalService.getCountApprovals(approvals);

        PurchaseRequestDTO pr = new PurchaseRequestDTO();
        pr.setCreatedBy(existingEmployee);
        Long prHeaderscount = purchaseRequestHeaderService.getCountPurchaseRequest(pr);

        PurchaseOrderDTO po = new PurchaseOrderDTO();
        po.setCreatedBy(existingEmployee);
        Long poHeaderscount = purchaseOrderHeaderService.getCountPurchaseOrder(po);

        if (auditLogsCount > 0) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated audit logs");
        }
        if (approvalCount > 0) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated requested approvals");
        }
        if (prHeaderscount>0) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated purchase request header");
        }

        if (poHeaderscount > 0) {
            throw new ResourceAlreadyUsedException("Cannot delete Employee with associated purchase order header");
        }

        auditLogsService.addAuditLog(EntityType.EMPLOYEE,employeeId,Action.DELETE,deleted);
        employeeRepository.deleteById(employeeId);
    }

    public List<Employee> getEmployeesList(EmployeeDTO employeeDTO) {

        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasEmployeeId(employeeDTO.getEmployeeId()))
                .and(EmployeeSpecification.hasEmployeeName(employeeDTO.getEmployeeName()))
                .and(EmployeeSpecification.hasDepartment(employeeDTO.getDepartment()))
                .and(EmployeeSpecification.hasRole(employeeDTO.getRole()))
                .and(EmployeeSpecification.hasActive(employeeDTO.getActive()));

        return employeeRepository.findAll(spec);
    }

    public Long getCountEmployees(EmployeeDTO employeeDTO) {

        Specification<Employee> spec = Specification
                .where(EmployeeSpecification.hasEmployeeId(employeeDTO.getEmployeeId()))
                .and(EmployeeSpecification.hasEmployeeName(employeeDTO.getEmployeeName()))
                .and(EmployeeSpecification.hasDepartment(employeeDTO.getDepartment()))
                .and(EmployeeSpecification.hasRole(employeeDTO.getRole()))
                .and(EmployeeSpecification.hasActive(employeeDTO.getActive()));

        return employeeRepository.count(spec);
    }
}
