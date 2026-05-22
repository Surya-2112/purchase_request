package com.module.purchase.view.purchaseOrder;

import java.util.List;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-details/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseOrderHeaderService headerService;
    private final PurchaseOrderLineService lineService;
    private final AssigningApprovalsService approvalsService;

    private PurchaseOrderHeader header;

    // ================= HEADER =================
    private final Span orderId = new Span();
    private final Span requestId = new Span();
    private final Span createdBy = new Span();
    private final Span department = new Span();
    private final Span vendor = new Span();
    private final Span totalAmount = new Span();
    private final Span createdDate = new Span();
    private final Span status = new Span();

    // ================= ACTIONS =================
    private final HorizontalLayout actionLayout = new HorizontalLayout();

    // ================= GRIDS =================
    private final Grid<PurchaseOrderLine> lineGrid = new Grid<>(PurchaseOrderLine.class, false);
    private final Grid<AssigningApprovals> approvalGrid = new Grid<>(AssigningApprovals.class, false);

    public PurchaseOrderDetailsView(
            PurchaseOrderHeaderService headerService,
            PurchaseOrderLineService lineService,
            AssigningApprovalsService approvalsService
    ) {
        this.headerService = headerService;
        this.lineService = lineService;
        this.approvalsService = approvalsService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        configureGrids();

        VerticalLayout headerSection = buildHeaderSection();
        headerSection.setWidthFull();

        VerticalLayout content = new VerticalLayout(
                new H2("Purchase Order Details"),
                headerSection,
                actionLayout,
                new H3("Line Items"),
                lineGrid //,
               // new H3("Approval Flow"),
              //  approvalGrid
        );

        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(true);

        // ✅ IMPORTANT: scroll fix
        Scroller scroller = new Scroller(content);
        scroller.setSizeFull();

        add(scroller);
        setSizeFull();
    }

    // =========================================================
    // LOAD
    // =========================================================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id = Long.parseLong(
                event.getRouteParameters().get("id").get()
        );

        header = headerService.getPurchaseOrderHeaderById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        bindHeader();
        loadGrids();
        configureActions();
    }

    // =========================================================
    // HEADER UI
    // =========================================================

    private VerticalLayout buildHeaderSection() {

        VerticalLayout layout = new VerticalLayout(
                orderId,
                requestId,
                createdBy,
                department,
                vendor,
                totalAmount,
                createdDate,
                status
        );

        layout.setPadding(true);
        layout.setSpacing(false);

        layout.getStyle()
                .set("background", "#f9f9f9")
                .set("border", "1px solid #ddd")
                .set("border-radius", "8px");

        return layout;
    }

    private void bindHeader() {

        orderId.setText("PO ID : " + header.getPurchaseOrderId());

        requestId.setText("PR ID : " +
                (header.getPurchaseRequestHeader() != null
                        ? header.getPurchaseRequestHeader().getPurchaseRequestId()
                        : "-"));

        createdBy.setText("Created By : " +
                (header.getCreatedBy() != null
                        ? header.getCreatedBy().getEmployeeName()
                        : "-"));

        department.setText("Department : " +
                header.getPurchaseRequestHeader().getForDepartment().getDepartmentName());

        vendor.setText("Vendor : " +
                (header.getVendor() != null ? header.getVendor().getVendorName() : "-"));

        totalAmount.setText("Total : " + header.getTotalAmount());
        createdDate.setText("Created : " + header.getCreatedDate());
        status.setText("Status : " + header.getStatus());
    }

    // =========================================================
    // ACTIONS (LIKE PURCHASE REQUEST)
    // =========================================================

    private void configureActions() {

        actionLayout.removeAll();

        if (header.getStatus() == Status.WAITING_APPROVAL) {

            Button cancelBtn = new Button("Cancel Purchase Order");

            cancelBtn.addClickListener(e -> {

                header.setStatus(Status.CANCELLED);

                headerService.updatePurchaseOrderHeader(
                        header,
                        header.getCreatedBy()
                );

                Notification.show("Purchase Order Cancelled");

                bindHeader();
                configureActions();
            });

            actionLayout.add(cancelBtn);
        }
    }

    // =========================================================
    // GRID CONFIG
    // =========================================================

    private void configureGrids() {

        // ================= LINE ITEMS =================
        lineGrid.addColumn(l ->
                        l.getItem() != null ? l.getItem().getItemName() : "")
                .setHeader("Item");

        lineGrid.addColumn(PurchaseOrderLine::getQuantity)
                .setHeader("Qty");

        lineGrid.addColumn(PurchaseOrderLine::getUnitPrice)
                .setHeader("Unit Price");

        lineGrid.addColumn(PurchaseOrderLine::getTotalPrice)
                .setHeader("Total");

        lineGrid.setWidthFull();
        lineGrid.setAllRowsVisible(true);

        // ================= APPROVAL =================
        approvalGrid.addColumn(AssigningApprovals::getLevel)
                .setHeader("Level");

        approvalGrid.addColumn(a ->
                        a.getApprover() != null
                                ? a.getApprover().getEmployeeName()
                                : "NOT ASSIGNED")
                .setHeader("Approver");

        approvalGrid.addColumn(AssigningApprovals::getAssignedDate)
                .setHeader("Assigned Date");

        approvalGrid.addColumn(a ->
                        a.getStatus() != null ? a.getStatus().name() : "")
                .setHeader("Status");

        approvalGrid.setWidthFull();
        approvalGrid.setAllRowsVisible(true);
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadGrids() {

        List<PurchaseOrderLine> lines =
                lineService.getPurchaseOrderLineByHeader(header);

        lineGrid.setItems(lines);

        List<AssigningApprovals> approvals =
                approvalsService.getAssigningApprovalByTypeAndReferId(
                        ApprovalType.PURCHASE_ORDER_APPROVAL,
                        header.getPurchaseOrderId()
                );

        approvalGrid.setItems(approvals);
    }
}