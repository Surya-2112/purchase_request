package com.module.purchase.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route("access-denied")
@PermitAll
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {
        add(
                new H2("Access Denied"),
                new Span("You don't have permission to access this page")
        );
    }
}