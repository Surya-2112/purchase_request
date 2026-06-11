package com.module.purchase.config;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.Users;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.repository.UsersRepository;
import com.module.purchase.service.ViewPermissionService;

@Service
public class SecurityService {

    private final UsersRepository userRepository;

    private final ViewPermissionService viewPermissionService;

    public SecurityService(UsersRepository userRepository, ViewPermissionService viewPermissionService ) {

        this.userRepository = userRepository;

        this.viewPermissionService = viewPermissionService;
    }

    public Users getLoggedInUser() {

        String username = SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return userRepository.findByUserNameOrUserEmail(username, username)
                .orElseThrow();
    }

    public boolean canAccessView(String viewName) {

        if(viewName.equals(""))
        {
           return true;
        }
        try {
            Users user = getLoggedInUser();

            if(user == null || user.getVendor() != null)
            {
                return viewPermissionService.getGroupsByView(viewName).contains(EmployeeGroup.VENDOR);
            }

            if (user == null || user.getEmployee() == null || user.getEmployee() .getRole() == null) {
                return false;
            }

            String roleName =user.getEmployee()
                            .getRole()
                            .getRoleName();


            if ("SUPER_ADMIN".equals(roleName)) {
                return true;
            }

            List<EmployeeGroup> userGroups =user.getEmployee().getRole().getEmployeeGroups();

            List<EmployeeGroup> allowedGroups = viewPermissionService.getGroupsByView(viewName);

            if (allowedGroups == null|| allowedGroups.isEmpty()) {

                return false;
            }

            return allowedGroups.stream().allMatch(userGroups::contains);

        } catch (Exception exception) {

            exception.printStackTrace();

            return false;
        }
    }
}