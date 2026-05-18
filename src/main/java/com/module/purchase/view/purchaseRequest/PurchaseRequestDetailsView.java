package com.module.purchase.view.purchaseRequest;

import com.module.purchase.entity.*;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.service.*;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-details/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseRequestHeaderService headerService;
    private final AssigningApprovalsService assigningApprovalsService;
    private final PurchaseRequestLineService purchaseRequestLineService;

    private PurchaseRequestHeader header;

    // HEADER UI
    private final Span requestId = new Span();
    private final Span createdBy = new Span();
    private final Span department = new Span();
    private final Span totalAmount = new Span();
    private final Span createdDate = new Span();
    private final Span status = new Span();

    // GRIDS
    private final Grid<PurchaseRequestLine> lineGrid =
            new Grid<>(PurchaseRequestLine.class, false);

    private final Grid<AssigningApprovals> approvalGrid =
            new Grid<>(AssigningApprovals.class, false);

    public PurchaseRequestDetailsView(
            PurchaseRequestHeaderService headerService,
            AssigningApprovalsService assigningApprovalsService,
            PurchaseRequestLineService purchaseRequestLineService) {

        this.headerService = headerService;
        this.assigningApprovalsService = assigningApprovalsService;
        this.purchaseRequestLineService = purchaseRequestLineService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        configureGrids();

        // HEADER SECTION
        VerticalLayout headerSection = buildHeaderSection();
        headerSection.setWidthFull();

        // MAIN CONTENT LAYOUT
        VerticalLayout content = new VerticalLayout(
                new H2("Purchase Request Details"),
                headerSection,
                new H3("Line Items"),
                lineGrid,
                new H3("Approval Flow"),
                approvalGrid
        );

        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(true);

        Scroller scroller = new Scroller(content);
        scroller.setSizeFull();

        add(scroller);
    }

    // ================= ROUTE LOAD =================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id = Long.parseLong(event.getRouteParameters().get("id").get());

        header = headerService.getPurchaseRequestHeaderById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        bindHeader();
        loadGrids();
    }

    // ================= HEADER =================

    private VerticalLayout buildHeaderSection() {

        VerticalLayout layout = new VerticalLayout(
                requestId,
                createdBy,
                department,
                totalAmount,
                createdDate,
                status
        );

        layout.setPadding(true);
        layout.setSpacing(false);
        layout.setWidthFull();
        layout.getStyle()
                .set("background", "#f9f9f9")
                .set("border", "1px solid #ddd")
                .set("border-radius", "8px");

        return layout;
    }

    private void bindHeader() {

        requestId.setText("Request ID: " + header.getPurchaseRequestId());

        createdBy.setText("Created By: " +
                (header.getCreatedBy() != null
                        ? header.getCreatedBy().getEmployeeName()
                        : "-"));

        department.setText("Department: " +
                (header.getForDepartment() != null
                        ? header.getForDepartment().getDepartmentName()
                        : "-"));

        totalAmount.setText("Total Amount: " + header.getTotalAmount());

        createdDate.setText("Created Date: " + header.getCreatedDate());

        status.setText("Status: " + header.getStatus());
    }

    // ================= GRID CONFIG =================

    private void configureGrids() {

        // LINE GRID
        lineGrid.addColumn(PurchaseRequestLine::getPurchaseRequestLineId)
                .setHeader("Line ID");

        lineGrid.addColumn(l ->
                l.getItem() != null ? l.getItem().getItemName() : "")
                .setHeader("Item Name");

        lineGrid.addColumn(PurchaseRequestLine::getQuantity)
                .setHeader("Quantity");

        lineGrid.addColumn(PurchaseRequestLine::getUnitPrice)
                .setHeader("Unit Price");

        lineGrid.addColumn(PurchaseRequestLine::getDiscount)
                .setHeader("Discount");

        lineGrid.addColumn(PurchaseRequestLine::getTotalPrice)
                .setHeader("Total Price");

        lineGrid.setWidthFull();
        lineGrid.setAllRowsVisible(true);

        // APPROVAL GRID
        approvalGrid.addColumn(AssigningApprovals::getLevel)
                .setHeader("Level");

        approvalGrid.addColumn(a ->
                a.getApprover() != null ? a.getApprover().getEmployeeName() : "")
                .setHeader("Approver");

        approvalGrid.addColumn(AssigningApprovals::getAssignedDate)
                .setHeader("Approved Date");

        approvalGrid.addColumn(a ->
                a.getStatus() != null ? a.getStatus().name() : "")
                .setHeader("Status");

        approvalGrid.setWidthFull();
        approvalGrid.setAllRowsVisible(true);
    }

    // ================= LOAD DATA =================

    private void loadGrids() {

        lineGrid.setItems(
                purchaseRequestLineService
                        .getPurchaseRequestLineByHeader(header)
        );

        approvalGrid.setItems(
                assigningApprovalsService.getAssigningApprovalByTypeAndReferId(
                        ApprovalType.PURCHASE_REQUEST_APPROVAL,
                        header.getPurchaseRequestId()
                )
        );
    }
}