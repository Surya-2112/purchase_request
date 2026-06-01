package com.module.purchase.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.ViewPermission;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.ViewName;
import com.module.purchase.repository.ViewPermissionRepository;

@Service
public class ViewPermissionService {

    @Autowired
    private ViewPermissionRepository viewPermissionRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    public ViewPermission save(ViewPermission permission) {

        return viewPermissionRepository.save(permission);
    }

    public List<ViewPermission> getAll() {

        return viewPermissionRepository.findAll();
    }

    public void addPermission(ViewPermission permission,Employee employee) {

        boolean exists = viewPermissionRepository.existsByViewNameAndEmployeeGroup(
                permission.getViewName(),
                permission.getEmployeeGroup());
        if (exists) {
            throw new RuntimeException("Permission already exists");
        }
        permission=save(permission);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.VIEW_PERMISSION);
        log.setEntityId(permission.getId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }

    public void deleteById(Long id,Employee employee) {

        viewPermissionRepository.deleteById(id);
        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.VIEW_PERMISSION);
        log.setEntityId(id);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }

    public List<EmployeeGroup> getGroupsByView(String routeName) {
        try {
            ViewName viewName = getViewNameByRoute(routeName);
            if (viewName == null) {
                return new ArrayList<>();
            }
            List<ViewPermission> permissions = viewPermissionRepository.findByViewName(viewName);
            List<EmployeeGroup> groups = new ArrayList<>();
            for (ViewPermission permission : permissions) {
                groups.add(permission.getEmployeeGroup());
            }
            return groups;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }}

    private ViewName getViewNameByRoute(String route) {
            for (ViewName viewName : ViewName.values()) {
            if (viewName.getRoute().equals(route)) {
                return viewName;}
        }

        return null;
    }

    public Page<ViewPermission> getAllPermissions(ViewName viewName, EmployeeGroup employeeGroup, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        ViewPermission probe = new ViewPermission();

        probe.setViewName(viewName);

        probe.setEmployeeGroup(employeeGroup);

        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreNullValues();

        Example<ViewPermission> example = Example.of(probe, matcher);

        return viewPermissionRepository.findAll(example, pageable);
    }
}