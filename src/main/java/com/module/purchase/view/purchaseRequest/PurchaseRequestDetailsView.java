package com.module.purchase.view.purchaseRequest;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-details", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestDetailsView extends VerticalLayout
        implements HasUrlParameter<Long> {

    private final PurchaseRequestHeaderService purchaseRequestHeaderService;

    public PurchaseRequestDetailsView(
            PurchaseRequestHeaderService purchaseRequestHeaderService) {

        this.purchaseRequestHeaderService =
                purchaseRequestHeaderService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);
    }

    @Override
    public void setParameter(
            BeforeEvent event,
            Long purchaseRequestId) {

        removeAll();

        PurchaseRequestHeader purchaseRequest =
                purchaseRequestHeaderService
                        .getPurchaseRequestHeaderById(
                                purchaseRequestId)
                        .orElse(null);

        if (purchaseRequest == null) {

            add(new Span("Purchase Request Not Found"));

            return;
        }

        // ================= TITLE =================

        H2 title =
                new H2("Purchase Request Details");

        // ================= HEADER DETAILS =================

        FormLayout formLayout =
                new FormLayout();

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                purchaseRequest.getPurchaseRequestId())),
                "Request ID");

        formLayout.addFormItem(
                new Span(
                        purchaseRequest.getCreatedBy() == null
                                ? ""
                                : purchaseRequest
                                        .getCreatedBy()
                                        .getEmployeeName()),
                "Created By");

        formLayout.addFormItem(
                new Span(
                        purchaseRequest.getCreatedDate() == null
                                ? ""
                                : purchaseRequest
                                        .getCreatedDate()
                                        .toString()),
                "Created Date");

        formLayout.addFormItem(
                new Span(
                        purchaseRequest.getForDepartment() == null
                                ? ""
                                : purchaseRequest
                                        .getForDepartment()
                                        .getDepartmentName()),
                "Department");

        formLayout.addFormItem(
                new Span(
                        purchaseRequest.getStatus() == null
                                ? ""
                                : purchaseRequest
                                        .getStatus()
                                        .name()),
                "Status");

        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                purchaseRequest.getTotalAmount())),
                "Total Amount");

        formLayout.addFormItem(
                new Span(
                        String.valueOf(
                                purchaseRequest.getLevel())),
                "Level");

        // ================= LINE GRID =================

        H3 lineTitle =
                new H3("Purchase Request Lines");

        Grid<PurchaseRequestLine> lineGrid =
                new Grid<>(PurchaseRequestLine.class, false);

        lineGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS);

        lineGrid.addColumn(line ->
                line.getItem() == null
                        ? ""
                        : line.getItem().getItemName())
                .setHeader("Item")
                .setAutoWidth(true);

        lineGrid.addColumn(
                PurchaseRequestLine::getQuantity)
                .setHeader("Quantity")
                .setAutoWidth(true);

        lineGrid.addColumn(
                PurchaseRequestLine::getUnitPrice)
                .setHeader("Unit Price")
                .setAutoWidth(true);

        lineGrid.addColumn(
                PurchaseRequestLine::getDiscount)
                .setHeader("Discount")
                .setAutoWidth(true);

        lineGrid.addColumn(
                PurchaseRequestLine::getTotalPrice)
                .setHeader("Total")
                .setAutoWidth(true);

        lineGrid.setItems(
                purchaseRequest.getPurchaseRequestLines());

        lineGrid.setWidthFull();

        lineGrid.setHeight("300px");

        // ================= APPROVAL GRID =================

        H3 approvalTitle =
                new H3("Assigning Approvals");

        Grid<AssigningApprovals> approvalGrid =
                new Grid<>(AssigningApprovals.class, false);

        approvalGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS);

        approvalGrid.addColumn(
                AssigningApprovals::getLevel)
                .setHeader("Level")
                .setAutoWidth(true);

        approvalGrid.addColumn(approval ->
                approval.getApprover() == null
                        ? ""
                        : approval.getApprover()
                                .getEmployeeName())
                .setHeader("Approver")
                .setAutoWidth(true);

        approvalGrid.addColumn(approval ->
                approval.getAssignedBy() == null
                        ? ""
                        : approval.getAssignedBy()
                                .getEmployeeName())
                .setHeader("Assigned By")
                .setAutoWidth(true);

        approvalGrid.addColumn(approval ->
                approval.getApprovalType() == null
                        ? ""
                        : approval.getApprovalType()
                                .name())
                .setHeader("Approval Type")
                .setAutoWidth(true);

        approvalGrid.addColumn(approval ->
                approval.getStatus() == null
                        ? ""
                        : approval.getStatus()
                                .name())
                .setHeader("Status")
                .setAutoWidth(true);

        approvalGrid.addColumn(approval ->
                approval.getAssignedDate() == null
                        ? ""
                        : approval.getAssignedDate()
                                .toString())
                .setHeader("Assigned Date")
                .setAutoWidth(true);

        approvalGrid.addColumn(approval ->
                approval.getApprovedDate() == null
                        ? ""
                        : approval.getApprovedDate()
                                .toString())
                .setHeader("Approved Date")
                .setAutoWidth(true);

        approvalGrid.addColumn(
                AssigningApprovals::getComments)
                .setHeader("Comments")
                .setAutoWidth(true);

        approvalGrid.setItems(
                purchaseRequest.getAssigningApprovals());

        approvalGrid.setWidthFull();

        approvalGrid.setHeight("300px");

        // ================= BUTTONS =================

        Button updateButton =
                new Button("Update");

        updateButton.addClickListener(clickEvent -> {

            getUI().ifPresent(ui ->

                    ui.navigate(
                            "purchase-request-edit/"
                                    + purchaseRequest
                                            .getPurchaseRequestId()));
        });

        Button deleteButton =
                new Button("Delete");

        deleteButton.addClickListener(clickEvent -> {

            ConfirmDialog dialog =
                    new ConfirmDialog();

            dialog.setHeader(
                    "Delete Purchase Request");

            dialog.setText(
                    "Are you sure you want to delete this purchase request?");

            dialog.setCancelable(true);

            dialog.setConfirmText("Delete");

            dialog.setConfirmButtonTheme(
                    "error primary");

            dialog.addConfirmListener(confirmEvent -> {

                try {

                    purchaseRequestHeaderService.deletePurchaseRequestHeaderById( purchaseRequest.getPurchaseRequestId());

                    Notification.show(
                            "Purchase Request Deleted Successfully");

                    getUI().ifPresent(ui ->

                            ui.navigate("purchase-request"));

                } catch (Exception exception) {

                    Notification.show(
                            exception.getMessage(),
                            5000,
                            Notification.Position.TOP_CENTER);
                }
            });

            dialog.open();
        });

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        updateButton,
                        deleteButton);

        // ================= ADD =================

        add(
                title,
                formLayout,
                lineTitle,
                lineGrid,
                approvalTitle,
                approvalGrid,
                buttonLayout);
    }
}