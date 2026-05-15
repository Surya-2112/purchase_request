package com.module.purchase.view.purchaseOrder;

import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "PurchaseOrder", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderView extends VerticalLayout{
    
}
