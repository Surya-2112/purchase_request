package com.module.purchase.service;

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
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Role;
import com.module.purchase.entityDTO.EmployeeDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.EntityType;
import com.module.purchase.repository.RoleRepository;
import com.module.purchase.specification.RoleSpecification;


@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    private EmployeeService employeeService;

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    public Role addRole(Role role,Employee employee) {
        Optional<Role> existingRole = roleRepository.findByRoleName(role.getRoleName());
        if (existingRole.isPresent()) {
            throw new ResourceAlreadyUsedException("Role already exists with name: " + role.getRoleName());
        }  
        role=saveRole(role);
        auditLogsService.addAuditLog(EntityType.ROLE,role.getRoleId(),Action.CREATE,employee);

        return role;
    }

    public Optional<Role> getRoleById(Long id) {
        Optional<Role> existingRole = roleRepository.findById(id);
        if (!existingRole.isPresent()) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        return existingRole;
    }

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    public Page<Role> getAllRoles(Role role,int page,int size)
    {   EmployeeGroup employeeGroup =  null;
        if(role.getEmployeeGroups()!=null)
        {
            employeeGroup=role.getEmployeeGroups().get(0);
        }

        Specification<Role> spec=Specification
        .where(RoleSpecification.hasRoleId(role.getRoleId()))
        .and(RoleSpecification.hasRoleName(role.getRoleName()))
        .and(RoleSpecification.hasEmployeeGroup(employeeGroup));

        Pageable pageable = PageRequest.of(page,size);
        Page<Role> pageRole = roleRepository.findAll(spec,pageable);
        return pageRole;
    }

    public Role updateRole(Role role,Employee employee)
    {   Role exist=getRoleById(role.getRoleId()).get();
        if(!exist.getRoleName().equals(role.getRoleName()))
        {
            throw new ModificationNotAllowedException("Role name not allowed to modified");
        }
        role=saveRole(role);
        auditLogsService.addAuditLog(EntityType.ROLE,role.getRoleId(),Action.UPDATE,employee);

        return role;
    }

    public void deleteRoleById(Long roleId,Employee employee)
    { 
        Role existingRole= getRoleById(roleId).get();
        EmployeeDTO employeeDTO =new EmployeeDTO();
        employeeDTO.setRole(existingRole);
        if(employeeService.getCountEmployees(employeeDTO)>0)
        {
            throw new ResourceAlreadyUsedException("Cannot delete Role with associated employee");
        }
        roleRepository.deleteById(roleId);
        auditLogsService.addAuditLog(EntityType.ROLE,roleId,Action.DELETE,employee);
    }
}
