package com.module.purchase.view.purchaseRequest;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.enums.ApprovalSource;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-approval/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestApprovalView extends VerticalLayout
        implements BeforeEnterObserver {

    private final PurchaseRequestHeaderService headerService;

    private final AssigningConfigService configService;

    private final EmployeeService employeeService;

    private final AssigningApprovalsService assigningApprovalsService;

    private final SecurityService securityService;

    private final DepartmentBudgetService departmentBudgetService;

    private PurchaseRequestHeader header;

    private final List<AssigningApprovals> approvals =
            new ArrayList<>();

    private final Grid<AssigningApprovals> grid =
            new Grid<>(AssigningApprovals.class, false);

    // ================= BUDGET UI =================

    private final H2 budgetTitle = new H2();

    private final Span yearSpan = new Span();

    private final Span totalBudgetSpan = new Span();

    private final Span remainingBudgetSpan = new Span();

    // ================= CONSTRUCTOR =================

    public PurchaseRequestApprovalView(
            PurchaseRequestHeaderService headerService,
            AssigningConfigService configService,
            EmployeeService employeeService,
            SecurityService securityService,
            AssigningApprovalsService assigningApprovalsService,
            DepartmentBudgetService departmentBudgetService) {

        this.headerService = headerService;

        this.configService = configService;

        this.employeeService = employeeService;

        this.securityService = securityService;

        this.assigningApprovalsService =
                assigningApprovalsService;

        this.departmentBudgetService =
                departmentBudgetService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        configureGrid();

        Button addBtn =
                new Button("Add Line", e -> addLine());

        Button saveBtn =
                new Button("Save", e -> saveAll());

        // ================= BUDGET LAYOUT =================

        VerticalLayout budgetLayout =
                new VerticalLayout(

                        budgetTitle,

                        yearSpan,

                        totalBudgetSpan,

                        remainingBudgetSpan
                );

        budgetLayout.setPadding(true);

        budgetLayout.setSpacing(false);

        budgetLayout.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "8px")
                .set("background", "#f9f9f9");

        add(

                new H2("Purchase Request Approval Setup"),

                budgetLayout,

                new HorizontalLayout(addBtn, saveBtn),

                grid
        );
    }

    // ================= BEFORE ENTER =================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        try {

            Long id = Long.parseLong(

                    event.getRouteParameters()
                            .get("id")
                            .get()
            );

            header = headerService

                    .getPurchaseRequestHeaderById(id)

                    .orElseThrow(() ->

                            new RuntimeException(
                                    "Request not found"
                            )
                    );

            // ================= CHECK DEPARTMENT =================

            if (header.getForDepartment() == null) {

                Notification.show(
                        "Department not found",
                        3000,
                        Position.TOP_CENTER
                );

                event.rerouteTo("purchase-request");

                return;
            }

            // ================= CHECK BUDGET =================

            DepartmentBudget budget =
                    departmentBudgetService
                            .getByDepartmentAndYear(

                                    header.getForDepartment(),

                                    Year.now()
                            );

            // ================= NO BUDGET =================

            if (budget == null) {

                Notification.show(
                        "Department Budget Not Configured",
                        3000,
                        Position.TOP_CENTER
                );

                event.rerouteTo("purchase-request");

                return;
            }

            // ================= SHOW BUDGET =================

            loadDepartmentBudget(budget);

            // ================= LOAD APPROVALS =================

            loadAutoApprovals();

        } catch (Exception e) {

            Notification.show(
                    "Department Budget Not Configured",
                    3000,
                    Position.TOP_CENTER
            );

            event.rerouteTo("purchase-request");
        }
    }

    // ================= LOAD BUDGET =================

    private void loadDepartmentBudget(
            DepartmentBudget budget) {

        budgetTitle.setText(
                "Department Budget"
        );

        yearSpan.setText(
                "Year : "
                        + budget.getYear()
        );

        totalBudgetSpan.setText(
                "Total Budget Amount : "
                        + budget.getTotalBudgetAmount()
        );

        remainingBudgetSpan.setText(
                "Remaining Budget Amount : "
                        + budget.getRemainingBudgetAmount()
        );
    }

    // ================= GRID =================

    private void configureGrid() {

        // ================= LEVEL =================

        grid.addColumn(AssigningApprovals::getLevel)
                .setHeader("Level")
                .setAutoWidth(true);

        // ================= APPROVER =================

        grid.addComponentColumn(item -> {

            ComboBox<Employee> combo =
                    new ComboBox<>();

            combo.setWidthFull();

            combo.setItemLabelGenerator(
                    Employee::getEmployeeName
            );

            // ================= AUTO =================

            if (item.getSource()
                    == ApprovalSource.AUTO) {

                List<AssigningConfig> configs =

                        configService.getConfigs(

                                ApprovalType
                                        .PURCHASE_REQUEST_APPROVAL,

                                header.getTotalAmount()
                        );

                AssigningConfig config =
                        configs.stream()

                                .filter(c ->
                                        c.getLevel()
                                                .equals(
                                                        item.getLevel()
                                                )
                                )

                                .findFirst()

                                .orElse(null);

                if (config != null) {

                    combo.setItems(

                            employeeService
                                    .getEmployeesByEmployeeGroup(

                                            config
                                                    .getEmployeeGroup()
                                    )
                    );

                } else {

                    combo.setItems(List.of());
                }

            } else {

                // ================= MANUAL =================

                combo.setItems(
                        employeeService.getEmployees()
                );
            }

            combo.setValue(item.getApprover());

            combo.addValueChangeListener(e ->

                    item.setApprover(
                            e.getValue()
                    )
            );

            return combo;

        }).setHeader("Approver");

        // ================= SOURCE =================

        grid.addColumn(a ->

                        a.getSource() != null

                                ? a.getSource().name()

                                : ""
                )

                .setHeader("Source");

        // ================= DELETE =================

        grid.addComponentColumn(item -> {

            Button delete =
                    new Button("Delete");

            delete.addClickListener(e -> {

                if (item.getSource()
                        == ApprovalSource.AUTO) {

                    Notification.show(
                            "Cannot delete AUTO approvals"
                    );

                    return;
                }

                approvals.remove(item);

                grid.setItems(approvals);

                Notification.show(
                        "Approval line deleted"
                );
            });

            delete.setEnabled(

                    item.getSource()
                            != ApprovalSource.AUTO
            );

            return delete;

        }).setHeader("Action");

        grid.setItems(approvals);

        grid.setWidthFull();

        grid.setAllRowsVisible(true);
    }

    // ================= AUTO APPROVAL =================

    private void loadAutoApprovals() {

        approvals.clear();

        List<AssigningConfig> configs =

                configService.getConfigs(

                        ApprovalType
                                .PURCHASE_REQUEST_APPROVAL,

                        header.getTotalAmount()
                );

        for (AssigningConfig c : configs) {

            AssigningApprovals approval =
                    new AssigningApprovals();

            approval.setLevel(
                    c.getLevel()
            );

            approval.setStatus(
                    Status.DRAFT
            );

            approval.setApprovalType(

                    ApprovalType
                            .PURCHASE_REQUEST_APPROVAL
            );

            approval.setSource(
                    ApprovalSource.AUTO
            );

            approvals.add(approval);
        }

        grid.setItems(approvals);
    }

    // ================= ADD LINE =================

    private void addLine() {

        int nextLevel = approvals.stream()

                .map(AssigningApprovals::getLevel)

                .filter(Objects::nonNull)

                .max(Integer::compareTo)

                .orElse(0) + 1;

        AssigningApprovals approval =
                new AssigningApprovals();

        approval.setLevel(nextLevel);

        approval.setStatus(Status.DRAFT);

        approval.setApprovalType(

                ApprovalType
                        .PURCHASE_REQUEST_APPROVAL
        );

        approval.setSource(
                ApprovalSource.MANUAL
        );

        approvals.add(approval);

        grid.setItems(approvals);

        Notification.show("Added level " + nextLevel);
    }

    // ================= SAVE =================

    private void saveAll() {

        // ================= EMPTY VALIDATION =================

        if (approvals.isEmpty()) {

            Notification.show(
                    "At least one approval line is required"
            );

            return;
        }

        Set<Integer> levels = new HashSet<>();

        Set<Long> approverIds = new HashSet<>();

        // ================= VALIDATION =================

        for (AssigningApprovals approval : approvals) {

            // APPROVER REQUIRED
            if (approval.getApprover() == null) {

                Notification.show(

                        "Approver missing for level "
                                + approval.getLevel()
                );

                return;
            }

            // DUPLICATE LEVEL
            if (!levels.add(
                    approval.getLevel()
            )) {

                Notification.show(

                        "Duplicate level found : "
                                + approval.getLevel()
                );

                return;
            }

            // DUPLICATE APPROVER
            if (approval.getApprover()
                    .getEmployeeId() != null) {

                boolean added = approverIds.add(

                        approval.getApprover()
                                .getEmployeeId()
                );

                if (!added) {

                    Notification.show(

                            "Duplicate approver : "
                                    + approval
                                    .getApprover()
                                    .getEmployeeName()
                    );

                    return;
                }
            }
        }

        // ================= SAVE APPROVALS =================

        for (AssigningApprovals approval : approvals) {

            approval.setReferenceId(

                    header.getPurchaseRequestId()
            );

            approval.setStatus(
                    Status.WAITING_APPROVAL
            );

            approval.setAssignedDate(
                    LocalDate.now()
            );

            approval.setAssignedBy(

                    securityService
                            .getLoggedInUser()
                            .getEmployee()
            );

            assigningApprovalsService.addApprovals(

                    approval,

                    approval.getAssignedBy()
            );
        }

        // ================= UPDATE HEADER =================

        header.setLevel(
                approvals.size()
        );

        header.setAssigningApprovals(
                approvals
        );

        header.setStatus(
                Status.WAITING_APPROVAL
        );

        headerService.updatePurchaseRequestHeader(

                header,

                securityService
                        .getLoggedInUser()
                        .getEmployee()
        );

        Notification.show(
                "Approval setup saved successfully"
        );

        getUI().ifPresent(ui ->
                ui.navigate("purchase-request")
        );
    }
}