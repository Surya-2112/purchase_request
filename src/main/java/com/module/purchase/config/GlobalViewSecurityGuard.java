package com.module.purchase.config;

import java.util.List;

import org.springframework.stereotype.Component;

import com.module.purchase.entity.Users;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.ViewPermissionService;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;

@Component
public class GlobalViewSecurityGuard implements BeforeEnterListener {

    private final SecurityService securityService;

    private final ViewPermissionService viewPermissionService;

    public GlobalViewSecurityGuard( SecurityService securityService, ViewPermissionService viewPermissionService) {

        this.securityService = securityService;
        this.viewPermissionService = viewPermissionService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        try {

            String viewName = event.getLocation().getFirstSegment();

            System.out.println("Checking View Permission : "+ viewName);
            

            if (viewName.isEmpty() || viewName.equals("login")) {
                return ;
            }

            Users user = securityService.getLoggedInUser();

            List<EmployeeGroup> allowedGroups = viewPermissionService.getGroupsByView(viewName);

            if(user.getVendor()!=null)
            {
                if(!allowedGroups.contains(EmployeeGroup.VENDOR))
                { event.forwardTo("");

                event.getUI().access(() -> {

                    Notification.show(
                            "Access Denied",
                            3000,
                            Notification.Position.MIDDLE);
                });
                }
                return;
            }

            if (user == null|| user.getEmployee() == null || user.getEmployee().getRole() == null) {

                event.forwardTo("login");

                return;
            }
            String roleName = user.getEmployee()
                    .getRole()
                    .getRoleName();

            System.out.println( "Role : " + roleName);

            if ("SUPER_ADMIN".equals(roleName)) {

                return;
            }

            List<EmployeeGroup> userGroups = user.getEmployee()
                    .getRole()
                    .getEmployeeGroups();

        

            if (allowedGroups == null || allowedGroups.isEmpty()) {

                event.forwardTo("");

                event.getUI().access(() -> {

                    Notification.show(
                            "Access Denied",
                            3000,
                            Notification.Position.MIDDLE);
                });

                return;
            }

            boolean allowed = userGroups.stream()
                    .anyMatch(allowedGroups::contains);

            if (!allowed) {

                event.forwardTo("");

                event.getUI().access(() -> {

                    Notification.show(
                            "Access Denied",
                            3000,
                            Notification.Position.MIDDLE);
                });

                return ;
            }

        } catch (Exception exception) {

            exception.printStackTrace();

            event.forwardTo("");

            event.getUI().access(() -> {

                Notification.show(
                        "Access Denied",
                        3000,
                        Notification.Position.MIDDLE);
            });
        }
    }
}