package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.repository.RoleRepository;
import com.module.purchase.entity.Role;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.specification.RoleSpecification;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
 

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    public Role addRole(Role role) {
        Optional<Role> existingRole = roleRepository.findByRoleName(role.getRoleName());
        if (existingRole.isPresent()) {
            throw new RuntimeException("Role already exists with name: " + role.getRoleName());
        }  
        return saveRole(role);
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
        Page<Role> pageRole=roleRepository.findAll(spec,pageable);
        return pageRole;
    }

    public Role updateRole(Role role)
    {   getRoleById(role.getRoleId());
        return saveRole(role);
    }

    public void deleteRoleById(Long roleId)
    { 
        Role existingRole= getRoleById(roleId).get();
        if(existingRole.getEmployees() != null && !existingRole.getEmployees().isEmpty())
        {
            throw new ResourceAlreadyUsedException("Cannot delete Role with associated employee");
        }
        roleRepository.deleteById(roleId);
    }
}
