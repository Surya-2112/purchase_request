package com.module.purchase.view.user;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Users;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.UsersService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "user-details", layout = MainLayout.class)
@PermitAll
public class UsersDetailsView extends VerticalLayout implements HasUrlParameter<String> {

    private final UsersService usersService;
    private final SecurityService securityService;

     Users user;

    public UsersDetailsView( UsersService usersService, SecurityService securityService) {

        this.usersService = usersService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void setParameter(BeforeEvent event, String userId) {

        removeAll();
        try{
        if(securityService.getLoggedInUser().getUserId().equals(Long.parseLong(userId))||securityService.canAccessView("management-group"))
        {
        user = usersService.getUserById(Long.parseLong(userId)).orElse(null);
        }else{
            event.forwardTo("");
            event.getUI().access(() -> { Notification.show("Access Denied",3000,Notification.Position.MIDDLE);
            });
        }
        if (user == null) {
            add(new Span("User not found"));
            return;
        }

        H2 title = new H2("User Details");

        FormLayout formLayout = new FormLayout();

        formLayout.addFormItem( new Span(String.valueOf(user.getUserId())), "User ID");

        formLayout.addFormItem(new Span(user.getUserName()),"User Name");

        formLayout.addFormItem(new Span(user.getUserEmail()),"Email");

        String userType = "";

        if (user.getEmployee() != null) {
            userType = "Employee";
        } else if (user.getVendor() != null) {
            userType = "Vendor";
        }

        formLayout.addFormItem(new Span(userType),"User Type");

        String linkedTo = "";

        if (user.getEmployee() != null) {
            linkedTo = user.getEmployee().getEmployeeName();
        } else if (user.getVendor() != null) {
            linkedTo = user.getVendor().getVendorName();
        }

        formLayout.addFormItem(new Span(linkedTo),"Linked To");

        formLayout.addFormItem( new Span(Boolean.TRUE.equals(user.getActive()) ? "Active": "Inactive"),"Status");

        Button updateButton = new Button("Update");

        updateButton.addClickListener(e ->getUI().ifPresent(ui ->ui.navigate( "user-edit/" + user.getUserId())));

        Button deleteButton = new Button("Delete");

        deleteButton.addClickListener(e -> {

            ConfirmDialog dialog = new ConfirmDialog();

            dialog.setHeader("Delete User");

            dialog.setText( "Are you sure you want to delete this user?");

            dialog.setCancelable(true);

            dialog.setConfirmText("Delete");

            dialog.setConfirmButtonTheme("error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    usersService.deleteUsersById( user.getUserId(), securityService.getLoggedInUser() .getEmployee());
                    Notification.show("User deleted successfully");

                    getUI().ifPresent(ui -> ui.navigate("user"));

                } catch (Exception ex) {
                    Notification.show( ex.getMessage(),5000, Notification.Position.TOP_CENTER);
                }
            });

            dialog.open();
        });

        updateButton.setVisible( securityService.canAccessView("user-edit"));

        deleteButton.setVisible(securityService.canAccessView("user-form"));

        HorizontalLayout buttons =new HorizontalLayout( updateButton, deleteButton);

        add(title,formLayout,buttons);

        }catch (NumberFormatException e) {
            event.forwardTo(ViewName.USER.getRoute());
            event.getUI().access(() -> {
                Notification.show("url is not valid ," + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            });
            return;
        }catch (Exception ex) {
            event.forwardTo(ViewName.USER.getRoute());
            event.getUI().access(() -> {Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);});
            return;
        }
    }

}