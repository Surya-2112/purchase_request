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
public class GlobalViewSecurityGuard
        implements BeforeEnterListener {

    private final SecurityService securityService;

    private final ViewPermissionService viewPermissionService;

    public GlobalViewSecurityGuard(
            SecurityService securityService,
            ViewPermissionService viewPermissionService) {

        this.securityService = securityService;
        this.viewPermissionService = viewPermissionService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        try {

            // ================= CURRENT VIEW =================

            String viewName = event.getLocation()
                    .getFirstSegment();

            System.out.println(
                    "Checking View Permission : "
                            + viewName);

            // ================= COMMON VIEWS =================

            if (viewName.isEmpty()
                    || viewName.equals("login")) {

                return;
            }

            // ================= USER =================

            Users user = securityService.getLoggedInUser();

            if (user == null
                    || user.getEmployee() == null
                    || user.getEmployee().getRole() == null) {

                event.forwardTo("login");

                return;
            }

            // ================= ROLE =================

            String roleName = user.getEmployee()
                    .getRole()
                    .getRoleName();

            System.out.println(
                    "Role : "
                            + roleName);

            // ================= SUPER ADMIN =================

            if ("SUPER_ADMIN".equals(roleName)) {

                return;
            }

            // ================= USER GROUPS =================

            List<EmployeeGroup> userGroups = user.getEmployee()
                    .getRole()
                    .getEmployeeGroups();

            System.out.println(
                    "User Groups : "
                            + userGroups);

            // ================= VIEW GROUPS =================

            List<EmployeeGroup> allowedGroups = viewPermissionService
                    .getGroupsByView(viewName);

            System.out.println(
                    "Allowed Groups : "
                            + allowedGroups);

            // ================= NO CONFIG =================

            if (allowedGroups == null
                    || allowedGroups.isEmpty()) {

                event.forwardTo("");

                event.getUI().access(() -> {

                    Notification.show(
                            "No permission configured for this view",
                            3000,
                            Notification.Position.MIDDLE);
                });

                return;
            }

            // ================= CHECK ACCESS =================

            boolean allowed = userGroups.stream()
                    .anyMatch(allowedGroups::contains);

            // ================= ACCESS DENIED =================

            if (!allowed) {

                event.forwardTo("");

                event.getUI().access(() -> {

                    Notification.show(
                            "Access Denied",
                            3000,
                            Notification.Position.MIDDLE);
                });

                return;
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