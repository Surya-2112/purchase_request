package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.repository.RoleRepository;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Role;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;
import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.specification.RoleSpecification;

import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    public Role addRole(Role role,Employee employee) {
        Optional<Role> existingRole = roleRepository.findByRoleName(role.getRoleName());
        if (existingRole.isPresent()) {
            throw new RuntimeException("Role already exists with name: " + role.getRoleName());
        }  
        role=saveRole(role);
        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.ROLE);
        log.setEntityId(role.getRoleId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return role;
    }

    public Optional<Role> getRoleById(Long id) {
        Optional<Role> existingRole = roleRepository.findById(id);
        if (!existingRole.isPresent()) {
            throw new RuntimeException("Role not found with id: " + id);
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
            throw new RuntimeException("Role name not allowed to modified");
        }
        role=saveRole(role);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.ROLE);
        log.setEntityId(role.getRoleId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return role;
    }

    public void deleteRoleById(Long roleId,Employee employee)
    { 
        Role existingRole= getRoleById(roleId).get();
        if(existingRole.getEmployees() != null && !existingRole.getEmployees().isEmpty())
        {
            throw new ResourceAlreadyUsedException("Cannot delete Role with associated employee");
        }
        roleRepository.deleteById(roleId);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.ROLE);
        log.setEntityId(roleId);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }
}
