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
import com.vaadin.flow.component.notification.Notification.Position;
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
    private final Grid<AssigningApprovals> grid = new Grid<>(AssigningApprovals.class, false);

    private final H2 budgetTitle = new H2();
    private final Span yearSpan = new Span();
    private final Span totalBudgetSpan = new Span();
    private final Span remainingBudgetSpan = new Span();

    private final Button addBtn = new Button("Add Approver");
    private final Button saveBtn = new Button("Save");

    private boolean budgetMissing = false;

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
        this.assigningApprovalsService = assigningApprovalsService;
        this.departmentBudgetService = departmentBudgetService;

        setSizeFull();
        setPadding(true);

        configureGrid();

        addBtn.addClickListener(e -> addLine());
        saveBtn.addClickListener(e -> saveAll());

        add(
                new H2("Purchase Request Approval Setup"),
                buildBudgetLayout(),
                new HorizontalLayout(addBtn, saveBtn),
                grid);
    }

    private VerticalLayout buildBudgetLayout() {
        VerticalLayout layout = new VerticalLayout(
                budgetTitle, yearSpan, totalBudgetSpan, remainingBudgetSpan);

        layout.getStyle()
                .set("border", "1px solid #ddd")
                .set("padding", "10px");

        return layout;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        try {
            Long id = Long.valueOf(event.getRouteParameters().get("id").orElseThrow());

            header = headerService.getPurchaseRequestHeaderById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            if (header.getForDepartment() == null) {
                budgetMissing = true;
                showBudgetMissing("Department not found");
                disableActions();
                return;
            }

            DepartmentBudget budget = departmentBudgetService.getByDepartmentAndYear(
                    header.getForDepartment(),
                    Year.now());

            if (budget == null) {
                budgetMissing = true;
                showBudgetMissing("Department Budget Not Configured");
                disableActions();
                return;
            }

            loadDepartmentBudget(budget);
            loadAutoApprovals();

        } catch (Exception e) {
            budgetMissing = true;
            showBudgetMissing("Department Budget Not Configured");
            disableActions();
        }
    }

    private void showBudgetMissing(String msg) {
        Notification.show(msg, 4000, Position.TOP_CENTER);
        budgetTitle.setText(msg);
        yearSpan.setText("");
        totalBudgetSpan.setText("");
        remainingBudgetSpan.setText("");
    }

    private void disableActions() {
        addBtn.setEnabled(false);
        saveBtn.setEnabled(false);
    }

    private void enableActions() {
        addBtn.setEnabled(true);
        saveBtn.setEnabled(true);
    }

    private void loadDepartmentBudget(DepartmentBudget budget) {

        budgetTitle.setText("Department Budget");
        yearSpan.setText("Year: " + budget.getYear());
        totalBudgetSpan.setText("Total: " + budget.getTotalBudgetAmount());
        remainingBudgetSpan.setText("Remaining: " + budget.getRemainingBudgetAmount());

        enableActions();
    }

    private void configureGrid() {

        grid.addColumn(AssigningApprovals::getLevel).setHeader("Level");

        grid.addComponentColumn(item -> {
            ComboBox<Employee> combo = new ComboBox<>();
            combo.setItemLabelGenerator(Employee::getEmployeeName);

            if (item.getSource() == ApprovalSource.AUTO) {
                List<AssigningConfig> configs = configService.getConfigs(
                        ApprovalType.PURCHASE_REQUEST,
                        header != null ? header.getTotalAmount() : 0);

                AssigningConfig config = configs.stream()
                        .filter(c -> Objects.equals(c.getLevel(), item.getLevel()))
                        .findFirst().orElse(null);

                if (config != null) {
                    combo.setItems(
                            employeeService.getEmployeesByEmployeeGroup(
                                    config.getEmployeeGroup()));
                }
            } else {
                combo.setItems(employeeService.getEmployeesByEmployeeGroup(EmployeeGroup.ADMIN));
            }
            combo.setValue(item.getApprover());
            combo.addValueChangeListener(e -> item.setApprover(e.getValue()));
            return combo;
        }).setHeader("Approver");

        grid.addColumn(a -> a.getSource() != null ? a.getSource().name() : "")
                .setHeader("Source");

        grid.addComponentColumn(item -> {

            Button delete = new Button("Delete");
            delete.addClickListener(e -> {
                if (budgetMissing) {
                    Notification.show("Budget missing - action blocked");
                    return;
                }
                approvals.remove(item);
                grid.setItems(approvals);
            });

            if (item.getSource().equals(ApprovalSource.AUTO)) {

                delete.setVisible(false);
            }

            delete.setEnabled(!budgetMissing);

            return delete;
        }).setHeader("Action");

        grid.setItems(approvals);
        grid.setAllRowsVisible(true);
    }

    private void loadAutoApprovals() {

        approvals.clear();

        List<AssigningConfig> configs = configService.getConfigs(
                ApprovalType.PURCHASE_REQUEST,
                header.getTotalAmount());

        configs.sort( Comparator.comparing(AssigningConfig::getLevel));

        for (AssigningConfig config : configs) {
            AssigningApprovals approval = new AssigningApprovals();
            approval.setLevel(config.getLevel());
            approval.setStatus(Status.DRAFT);
            approval.setApprovalType(ApprovalType.PURCHASE_REQUEST);
            approval.setSource(ApprovalSource.AUTO);
            approvals.add(approval);
        }
        grid.setItems(approvals);
    }

    private void addLine() {

        if (budgetMissing) {
            Notification.show("Cannot add - budget not configured", 3000, Position.TOP_CENTER);
            return;
        }

        int next = approvals.stream()
                .map(AssigningApprovals::getLevel)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        AssigningApprovals a = new AssigningApprovals();
        a.setLevel(next);
        a.setStatus(Status.DRAFT);
        a.setApprovalType(ApprovalType.PURCHASE_REQUEST);
        a.setSource(ApprovalSource.MANUAL);

        approvals.add(a);
        grid.setItems(approvals);
    }

    private void saveAll() {

        if (budgetMissing) {
            Notification.show("Cannot save - Department Budget missing", 4000, Position.TOP_CENTER);
            return;
        }

        if (approvals.isEmpty()) {
            Notification.show("No approval lines");
            return;
        }

        for (AssigningApprovals a : approvals) {
            if (a.getApprover() == null) {
                Notification.show("Missing approver at level " + a.getLevel());
                return;
            }
        }

        for (AssigningApprovals a : approvals) {

            a.setReferenceId(header.getPurchaseRequestId());
            a.setStatus(Status.DRAFT);
            a.setAssignedDate(LocalDate.now());
            a.setAssignedBy(securityService.getLoggedInUser().getEmployee());

            assigningApprovalsService.addApprovals(a, a.getAssignedBy());
        }

        header.setStatus(Status.WAITING_APPROVAL);
        header.setLevel(approvals.size());
        headerService.updatePurchaseRequestHeader(header, securityService.getLoggedInUser().getEmployee());

        Notification.show("Saved successfully", 3000, Position.TOP_CENTER);

        getUI().ifPresent(ui -> ui.navigate("purchase-request"));
    }
}