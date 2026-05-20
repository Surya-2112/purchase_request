package com.module.purchase.view.purchaseRequest;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.*;
import com.module.purchase.enums.*;
import com.module.purchase.service.*;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-approval/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestApprovalView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseRequestHeaderService headerService;
    private final AssigningConfigService configService;
    private final EmployeeService employeeService;
    private final AssigningApprovalsService assigningApprovalsService;
    private final SecurityService securityService;
    private final DepartmentBudgetService departmentBudgetService;

    private PurchaseRequestHeader header;

    private final List<AssigningApprovals> approvals = new ArrayList<>();

    private final Grid<AssigningApprovals> grid =
            new Grid<>(AssigningApprovals.class, false);

    private final ComboBox<Employee> approverBox =
            new ComboBox<>("Approver");

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
        this.assigningApprovalsService = assigningApprovalsService;
        this.securityService = securityService;
        this.departmentBudgetService = departmentBudgetService;

        setSizeFull();

        approverBox.setItemLabelGenerator(Employee::getEmployeeName);

        configureGrid();

        Button addBtn = new Button("Add Line", e -> addLine());

        Button saveBtn = new Button("Save", e -> saveAll());

        // ================= BUDGET LAYOUT =================

        VerticalLayout budgetLayout = new VerticalLayout(
                budgetTitle,
                yearSpan,
                totalBudgetSpan,
                remainingBudgetSpan
        );

        budgetLayout.setPadding(true);
        budgetLayout.setSpacing(false);

        add(
                new H2("Purchase Request Approval Setup"),
                budgetLayout,
                new HorizontalLayout(addBtn, saveBtn),
                grid
        );
    }

    // ================= LOAD =================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id = Long.parseLong(
                event.getRouteParameters().get("id").get()
        );

        header = headerService.getPurchaseRequestHeaderById(id)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        loadDepartmentBudget();

        loadAutoApprovals();
    }

    // ================= LOAD BUDGET =================

    private void loadDepartmentBudget() {
        if (header.getForDepartment() == null) {
            return;
        }

        Department department = header.getForDepartment();
      
        DepartmentBudget budget =
                departmentBudgetService.getByDepartmentAndYear(
                        department,
                        Year.now()
                );

        if (budget == null) {

            budgetTitle.setText("Department Budget Not Configured");

            yearSpan.setText("");
            totalBudgetSpan.setText("");
            remainingBudgetSpan.setText("");

            return;
        }

        budgetTitle.setText("Department Budget");

        yearSpan.setText(
                "Year : " + budget.getYear()
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

        grid.addColumn(AssigningApprovals::getLevel)
                .setHeader("Level");

        // ================= APPROVER COLUMN =================

        grid.addComponentColumn(item -> {

            ComboBox<Employee> combo = new ComboBox<>();

            combo.setItemLabelGenerator(Employee::getEmployeeName);

            combo.setWidthFull();

            // AUTO approvals -> filtered by config employee group
            if (item.getSource() == ApprovalSource.AUTO) {

                List<AssigningConfig> configs =
                        configService.getConfigs(
                                ApprovalType.PURCHASE_REQUEST_APPROVAL,
                                header.getTotalAmount()
                        );

                AssigningConfig config = configs.stream()
                        .filter(c ->
                                c.getLevel().equals(item.getLevel()))
                        .findFirst()
                        .orElse(null);

                if (config != null) {

                    combo.setItems(
                            employeeService
                                    .getEmployeesByEmployeeGroup(
                                            config.getEmployeeGroup()
                                    )
                    );

                } else {

                    combo.setItems(List.of());
                }

            } else {

                // MANUAL approvals -> all employees
                combo.setItems(employeeService.getEmployees());
            }

            combo.setValue(item.getApprover());

            combo.addValueChangeListener(
                    e -> item.setApprover(e.getValue())
            );

            return combo;

        }).setHeader("Approver");

        // ================= SOURCE =================

        grid.addColumn(a ->
                        a.getSource() != null
                                ? a.getSource().name()
                                : "")
                .setHeader("Source");

        // ================= DELETE =================

        grid.addComponentColumn(item -> {

            Button delete = new Button("Delete", e -> {

                if (item.getSource() == ApprovalSource.AUTO) {

                    Notification.show(
                            "Cannot delete AUTO approvals"
                    );

                    return;
                }

                approvals.remove(item);

                grid.setItems(approvals);
            });

            delete.setEnabled(
                    item.getSource() != ApprovalSource.AUTO
            );

            return delete;

        }).setHeader("Action");

        grid.setItems(approvals);
    }

    // ================= AUTO LOAD =================

    private void loadAutoApprovals() {

        approvals.clear();

        List<AssigningConfig> configs =
                configService.getConfigs(
                        ApprovalType.PURCHASE_REQUEST_APPROVAL,
                        header.getTotalAmount()
                );

        for (AssigningConfig c : configs) {

            AssigningApprovals a =
                    new AssigningApprovals();

            a.setLevel(c.getLevel());

            a.setStatus(Status.DRAFT);

            a.setApprovalType(
                    ApprovalType.PURCHASE_REQUEST_APPROVAL
            );

            a.setSource(ApprovalSource.AUTO);

            approvals.add(a);
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

        AssigningApprovals a =
                new AssigningApprovals();

        a.setLevel(nextLevel);

        a.setStatus(Status.DRAFT);

        a.setApprovalType(
                ApprovalType.PURCHASE_REQUEST_APPROVAL
        );

        a.setSource(ApprovalSource.MANUAL);

        approvals.add(a);

        grid.setItems(approvals);

        Notification.show(
                "Added level " + nextLevel
        );
    }

    // ================= SAVE =================

    private void saveAll() {

        Set<Integer> levels = new HashSet<>();

        for (AssigningApprovals a : approvals) {

            if (a.getApprover() == null) {

                Notification.show(
                        "Approver missing for level "
                                + a.getLevel()
                );

                return;
            }

            if (!levels.add(a.getLevel())) {

                Notification.show(
                        "Duplicate level: "
                                + a.getLevel()
                );

                return;
            }
        }

        for (AssigningApprovals a : approvals) {

            a.setReferenceId(
                    header.getPurchaseRequestId()
            );

            a.setStatus(Status.DRAFT);

            a.setAssignedDate(LocalDate.now());

            a.setAssignedBy(
                    securityService
                            .getLoggedInUser()
                            .getEmployee()
            );

            assigningApprovalsService.addApprovals(a,a.getAssignedBy());
        }

        header.setLevel(approvals.size());

        header.setAssigningApprovals(approvals);

        header.setStatus(Status.WAITING_APPROVAL);

        headerService.updatePurchaseRequestHeader(header,securityService.getLoggedInUser().getEmployee());

        Notification.show("Saved successfully");

        getUI().ifPresent(
                ui -> ui.navigate("purchase-request")
        );
    }
}