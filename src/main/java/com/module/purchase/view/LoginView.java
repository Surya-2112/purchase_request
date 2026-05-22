package com.module.purchase.view;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.annotation.security.PermitAll;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
@PermitAll
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView() {

        loginForm = new LoginForm();

        loginForm.setAction("login");

        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);

        setAlignItems(Alignment.CENTER);

        add(loginForm);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        if (event.getLocation().getQueryParameters().getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }

        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("logout")) {

            Notification.show( "Logout successful");
        }
    }
}