package com.module.purchase.view.purchaseOrder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.*;
import com.module.purchase.enums.*;
import com.module.purchase.service.*;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-approval/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderApprovalView extends VerticalLayout
        implements BeforeEnterObserver {

    private final PurchaseOrderHeaderService poService;
    private final AssigningApprovalsService approvalsService;
    private final EmployeeService employeeService;
    private final SecurityService securityService;
    private final AssigningConfigService configService;

    private PurchaseOrderHeader header;

    private final List<AssigningApprovals> approvals = new ArrayList<>();
    private final Grid<AssigningApprovals> grid =
            new Grid<>(AssigningApprovals.class, false);

    private final Button addBtn = new Button("Add Line");
    private final Button saveBtn = new Button("Submit");

    public PurchaseOrderApprovalView(
            PurchaseOrderHeaderService poService,
            AssigningApprovalsService approvalsService,
            EmployeeService employeeService,
            SecurityService securityService,
            AssigningConfigService configService) {

        this.poService = poService;
        this.approvalsService = approvalsService;
        this.employeeService = employeeService;
        this.securityService = securityService;
        this.configService = configService;

        setSizeFull();
        setPadding(true);

        configureGrid();

        addBtn.addClickListener(e -> addLine());
        saveBtn.addClickListener(e -> saveApprovals());

        add(
                new H2("Purchase Order Approval Setup"),
                new HorizontalLayout(addBtn, saveBtn),
                grid
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id = Long.parseLong(
                event.getRouteParameters()
                        .get("id")
                        .orElseThrow()
        );

        header = poService.getPurchaseOrderHeaderById(id)
                .orElseThrow(() ->
                        new RuntimeException("Purchase Order Not Found")
                );

        loadAutoApprovals();
    }

    private void loadAutoApprovals() {

        approvals.clear();

        List<AssigningConfig> configs =
                configService.getConfigs(
                        ApprovalType.PURCHASE_ORDER_APPROVAL,
                        header.getTotalAmount()
                );

        for (AssigningConfig c : configs) {

            AssigningApprovals a = new AssigningApprovals();
            a.setLevel(c.getLevel());
            a.setStatus(Status.DRAFT);
            a.setApprovalType(ApprovalType.PURCHASE_ORDER_APPROVAL);
            a.setSource(ApprovalSource.AUTO);

            approvals.add(a);
        }

        grid.setItems(approvals);
    }

    // =========================================================
    // GRID CONFIG
    // =========================================================

    private void configureGrid() {

        grid.removeAllColumns();

        grid.addColumn(AssigningApprovals::getLevel)
                .setHeader("Level");

        grid.addComponentColumn(a -> {

            ComboBox<Employee> box = new ComboBox<>();

            if (a.getSource() == ApprovalSource.AUTO) {

                List<AssigningConfig> configs =
                        configService.getConfigs(
                                ApprovalType.PURCHASE_ORDER_APPROVAL,
                                header != null ? header.getTotalAmount() : 0
                        );

                AssigningConfig config = configs.stream()
                        .filter(c -> Objects.equals(c.getLevel(), a.getLevel()))
                        .findFirst()
                        .orElse(null);

                if (config != null) {
                    box.setItems(
                            employeeService.getEmployeesByEmployeeGroup(
                                    config.getEmployeeGroup()
                            )
                    );
                }

            } else {
                box.setItems(employeeService.getEmployees());
            }

            box.setItemLabelGenerator(Employee::getEmployeeName);
            box.setWidthFull();
            box.setValue(a.getApprover());

            box.addValueChangeListener(e ->
                    a.setApprover(e.getValue())
            );

            return box;

        }).setHeader("Approver");

        grid.addColumn(a ->
                        a.getSource() != null ? a.getSource().name() : "")
                .setHeader("Source");

        // =====================================================
        // DELETE ACTION
        // =====================================================

        grid.addComponentColumn(item -> {

            Button delete = new Button("Delete");

            delete.addClickListener(e -> {

                if (approvals.size() <= 1) {
                    Notification.show(
                            "At least one approval line is required",
                            3000,
                            Position.TOP_CENTER
                    );
                    return;
                }

                approvals.remove(item);
                grid.getDataProvider().refreshAll();
            });

            delete.setEnabled(true);

            return delete;

        }).setHeader("Action");

        grid.setItems(approvals);
        grid.setAllRowsVisible(true);
    }

    // =========================================================
    // ADD MANUAL LINE
    // =========================================================

    private void addLine() {

        int nextLevel = approvals.stream()
                .map(AssigningApprovals::getLevel)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        AssigningApprovals a = new AssigningApprovals();
        a.setLevel(nextLevel);
        a.setStatus(Status.DRAFT);
        a.setApprovalType(ApprovalType.PURCHASE_ORDER_APPROVAL);
        a.setSource(ApprovalSource.MANUAL);

        approvals.add(a);
        grid.setItems(approvals);
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void saveApprovals() {

        if (approvals.isEmpty()) {
            Notification.show(
                    "At least one approval line is required",
                    3000,
                    Position.TOP_CENTER
            );
            return;
        }

        Set<Integer> levels = new HashSet<>();

        for (AssigningApprovals a : approvals) {

            if (a.getApprover() == null) {
                Notification.show(
                        "Approver missing at level " + a.getLevel(),
                        3000,
                        Position.TOP_CENTER
                );
                return;
            }

            if (!levels.add(a.getLevel())) {
                Notification.show(
                        "Duplicate level found",
                        3000,
                        Position.TOP_CENTER
                );
                return;
            }
        }

        for (AssigningApprovals a : approvals) {

            a.setReferenceId(header.getPurchaseOrderId());
            a.setAssignedDate(LocalDate.now());
            a.setAssignedBy(
                    securityService.getLoggedInUser().getEmployee()
            );

            approvalsService.addApprovals(a, a.getAssignedBy());
        }

        header.setStatus(Status.WAITING_APPROVAL);
        header.setLevel(approvals.size());

        poService.updatePurchaseOrderHeader(
                header,
                securityService.getLoggedInUser().getEmployee()
        );

        Notification.show(
                "Purchase Order Submitted",
                3000,
                Position.TOP_CENTER
        );

        getUI().ifPresent(ui ->
                ui.navigate("purchase-order")
        );
    }
}