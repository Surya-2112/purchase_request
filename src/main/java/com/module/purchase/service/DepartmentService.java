package com.module.purchase.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceMissingFieldException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.Employee;
import com.module.purchase.entityDTO.DepartmentDTO;
import com.module.purchase.entityDTO.EmployeeDTO;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.mapper.DepartmentMapper;
import com.module.purchase.repository.DepartmentRepository;
import com.module.purchase.specification.DepartmentSpecification;

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

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    private DepartmentBudgetService departmentBudgetService;

    @Autowired
    private PurchaseRequestHeaderService purchaseRequestService;

    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public Department addDepartment(Department department,Employee employee) {
       
        Optional<Department> existingDepartment = departmentRepository.findByDepartmentCode(department.getDepartmentCode());
        if (existingDepartment.isPresent()) {
            throw new ResourceAlreadyUsedException("Department with code " + department.getDepartmentCode() + " already exists.");
        }
        Optional<Department> existingDepartmentname = departmentRepository.findByDepartmentName(department.getDepartmentName());
        if (existingDepartmentname.isPresent()) {
            throw new ResourceAlreadyUsedException("Department with Name " + department.getDepartmentName() + " already exists.");
        }
        saveDepartment(department);
        auditLogsService.addAuditLog(EntityType.DEPARTMENT,department.getDepartmentId(),Action.CREATE,employee);

        return department;
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

    public Department updateDepartment(Department department,Employee employee)
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

        EmployeeDTO emp=new EmployeeDTO();
        emp.setDepartment(existingDepartment);

        if(!department.getActive() && employeeSerivce.getCountEmployees(emp)>0)
        {
            throw new RuntimeException("Cannot inActive department because it associated with Employee");
        }
        saveDepartment(department);
        auditLogsService.addAuditLog(EntityType.DEPARTMENT,department.getDepartmentId(),Action.UPDATE,employee);
        return department;
    }

    public void deleteDepartmentById(Long departmentId,Employee employee)
    {
        Department existingDepartment=getDepartmentById(departmentId).get();

        List<DepartmentBudget> departmentbugets=departmentBudgetService.getDepartmentBudgetByDepartment(existingDepartment);

        EmployeeDTO emp=new EmployeeDTO();
        emp.setDepartment(existingDepartment);
        Long employeesCount=employeeSerivce.getCountEmployees(emp);

        PurchaseRequestDTO pr=new PurchaseRequestDTO();
        pr.setForDepartment(existingDepartment);
        Long prHeadersCount=purchaseRequestService.getCountPurchaseRequest(pr);

        if(departmentbugets!=null && !departmentbugets.isEmpty())
        {
            throw new ResourceAlreadyUsedException("Cannot delete department with associated department budgets");
        }
        if(employeesCount>0)
        {
            throw new ResourceAlreadyUsedException("Cannot delete department with associated employee");
        }
        if(prHeadersCount > 0)
        {
            throw new ResourceAlreadyUsedException("Cannot delete department with associated purchase request headers");
        }
        auditLogsService.addAuditLog(EntityType.DEPARTMENT,departmentId,Action.DELETE,employee);
        departmentRepository.deleteById(departmentId);
    }

    public List<Department> getAllDepartmentsList(DepartmentDTO departmentDTO)
    {
         Specification<Department> spec= Specification
        .where(DepartmentSpecification.hasDepartmentId(departmentDTO.getDepartmentId()))
        .and(DepartmentSpecification.hasDepartmentName(departmentDTO.getDepartmentName()))
        .and(DepartmentSpecification.hasDepartmentCode(departmentDTO.getDepartmentCode()))
        .and(DepartmentSpecification.hasActive(departmentDTO.getActive()));

        return departmentRepository.findAll(spec);
    }
}
