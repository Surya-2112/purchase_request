package com.module.purchase.view.purchaseRequest;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "assigning-approvals-details/:id", layout = MainLayout.class)
@PermitAll
public class AssigningApprovalsDetailsView extends VerticalLayout
        implements BeforeEnterObserver {

    private final PurchaseRequestHeaderService headerService;

    private final PurchaseRequestLineService lineService;

    private final AssigningApprovalsService approvalsService;

    private final DepartmentBudgetService departmentBudgetService;

    private final SecurityService securityService;

    private PurchaseRequestHeader header;

    private AssigningApprovals approval;

    // ================= HEADER =================

    private final Span requestId = new Span();

    private final Span createdBy = new Span();

    private final Span department = new Span();

    private final Span totalAmount = new Span();

    private final Span createdDate = new Span();

    private final Span level = new Span();

    private final Span status = new Span();

    // ================= BUDGET =================

    private final H3 budgetTitle = new H3();

    private final Span budgetYear = new Span();

    private final Span totalBudgetAmount = new Span();

    private final Span remainingBudgetAmount = new Span();

    // ================= COMMENTS =================

    private final TextArea comments =
            new TextArea("Comments");

    // ================= GRID =================

    private final Grid<PurchaseRequestLine> lineGrid =
            new Grid<>(PurchaseRequestLine.class, false);

    public AssigningApprovalsDetailsView(
            PurchaseRequestHeaderService headerService,
            PurchaseRequestLineService lineService,
            AssigningApprovalsService approvalsService,
            DepartmentBudgetService departmentBudgetService,
            SecurityService securityService) {

        this.headerService = headerService;
        this.lineService = lineService;
        this.approvalsService = approvalsService;
        this.departmentBudgetService = departmentBudgetService;
        this.securityService=securityService;

        setSizeFull();

        setPadding(false);

        setSpacing(false);

        comments.setWidthFull();

        comments.setMinHeight("120px");

        configureLineGrid();

        Button approveBtn = new Button("Approve");

        Button rejectBtn = new Button("Reject");

        approveBtn.addClickListener(e -> approveRequest());

        rejectBtn.addClickListener(e -> rejectRequest());

        HorizontalLayout buttonLayout =
                new HorizontalLayout(
                        approveBtn,
                        rejectBtn
                );

        VerticalLayout content = new VerticalLayout(

                new H2("Purchase Request Approval Details"),

                buildHeaderSection(),

                buildBudgetSection(),

                new H3("Line Items"),

                lineGrid,

                comments,

                buttonLayout
        );

        content.setWidthFull();

        content.setPadding(true);

        content.setSpacing(true);

        Scroller scroller = new Scroller(content);

        scroller.setSizeFull();

        add(scroller);
    }

    // ================= LOAD =================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long approvalId =
                Long.parseLong(
                        event.getRouteParameters()
                                .get("id")
                                .get()
                );

        approval = approvalsService
                .getAssigningApprovalById(approvalId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Approval not found"
                        ));

        header = headerService
                .getPurchaseRequestHeaderById(
                        approval.getReferenceId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Purchase Request not found"
                        ));

        bindHeader();

        loadDepartmentBudget();

        loadLines();
    }

    // ================= HEADER SECTION =================

    private VerticalLayout buildHeaderSection() {

        VerticalLayout layout =
                new VerticalLayout(

                        requestId,

                        createdBy,

                        department,

                        totalAmount,

                        createdDate,

                        level,

                        status
                );

        layout.setSpacing(false);

        layout.setPadding(false);

        return layout;
    }

    // ================= BUDGET SECTION =================

    private VerticalLayout buildBudgetSection() {

        VerticalLayout layout =
                new VerticalLayout(

                        budgetTitle,

                        budgetYear,

                        totalBudgetAmount,

                        remainingBudgetAmount
                );

        layout.setSpacing(false);

        layout.setPadding(false);

        return layout;
    }

    private void bindHeader() {

        requestId.setText(
                "Purchase Request ID : "
                        + header.getPurchaseRequestId()
        );

        createdBy.setText(
                "Created By : "
                        + (header.getCreatedBy() != null
                        ? header.getCreatedBy()
                        .getEmployeeName()
                        : "-")
        );

        department.setText(
                "Department : "
                        + (header.getForDepartment() != null
                        ? header.getForDepartment()
                        .getDepartmentName()
                        : "-")
        );

        totalAmount.setText(
                "Total Amount : "
                        + header.getTotalAmount()
        );

        createdDate.setText(
                "Created Date : "
                        + header.getCreatedDate()
        );

        level.setText(
                "Approval Level : "
                        + approval.getLevel()
        );

        status.setText(
                "Approval Status : "
                        + approval.getStatus()
        );
    }

    // ================= LOAD BUDGET =================

    private void loadDepartmentBudget() {

        if (header.getForDepartment() == null) {
            return;
        }

        Department dept =
                header.getForDepartment();

        DepartmentBudget budget =
                departmentBudgetService
                        .getByDepartmentAndYear(
                                dept,
                                Year.now()
                        );

        if (budget == null) {

            budgetTitle.setText(
                    "Department Budget Not Configured"
            );

            budgetYear.setText("");

            totalBudgetAmount.setText("");

            remainingBudgetAmount.setText("");

            return;
        }

        budgetTitle.setText("Department Budget");

        budgetYear.setText(
                "Year : "
                        + budget.getYear()
        );

        totalBudgetAmount.setText(
                "Total Budget Amount : "
                        + budget.getTotalBudgetAmount()
        );

        remainingBudgetAmount.setText(
                "Remaining Budget Amount : "
                        + budget.getRemainingBudgetAmount()
        );
    }

    // ================= GRID =================

    private void configureLineGrid() {

        lineGrid.setWidthFull();

        lineGrid.addColumn(
                PurchaseRequestLine::getPurchaseRequestLineId
        ).setHeader("Line ID");

        lineGrid.addColumn(line ->
                line.getItem() != null
                        ? line.getItem().getItemName()
                        : ""
        ).setHeader("Item Name");

        lineGrid.addColumn(
                PurchaseRequestLine::getQuantity
        ).setHeader("Quantity");

        lineGrid.addColumn(
                PurchaseRequestLine::getUnitPrice
        ).setHeader("Unit Price");

        lineGrid.addColumn(
                PurchaseRequestLine::getDiscount
        ).setHeader("Discount");

        lineGrid.addColumn(
                PurchaseRequestLine::getTotalPrice
        ).setHeader("Total Price");
    }

    private void loadLines() {

        List<PurchaseRequestLine> lines =
                lineService
                        .getPurchaseRequestLineByHeader(
                                header
                        );

        lineGrid.setItems(lines);
    }

    // ================= APPROVE =================

    private void approveRequest() {

        approval.setStatus(Status.APPROVED);

        approval.setComments(
                comments.getValue()
        );

        approval.setApprovedDate(
                LocalDate.now()
        );

        approvalsService.updateApprovals(approval,securityService.getLoggedInUser().getEmployee());

        Notification.show(
                "Purchase Request Approved"
        );

        getUI().ifPresent(ui ->
                ui.navigate("purchase-request")
        );
    }

    // ================= REJECT =================

    private void rejectRequest() {

        approval.setStatus(Status.REJECTED);

        approval.setComments(
                comments.getValue()
        );

        approval.setApprovedDate(
                LocalDate.now()
        );

        approvalsService.updateApprovals(approval,securityService.getLoggedInUser().getEmployee());

        Notification.show(
                "Purchase Request Rejected"
        );

        getUI().ifPresent(ui ->
                ui.navigate("purchase-request")
        );
    }
}