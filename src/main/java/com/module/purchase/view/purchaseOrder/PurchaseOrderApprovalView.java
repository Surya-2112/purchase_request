package com.module.purchase.view.purchaseOrder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.enums.ApprovalSource;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
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

    private PurchaseOrderHeader header;

    private final List<AssigningApprovals> approvals = new ArrayList<>();

    private final Grid<AssigningApprovals> grid = new Grid<>(AssigningApprovals.class, false);

    public PurchaseOrderApprovalView( PurchaseOrderHeaderService poService, AssigningApprovalsService approvalsService,
                                         EmployeeService employeeService, SecurityService securityService) {

        this.poService = poService;
        this.approvalsService = approvalsService;
        this.employeeService = employeeService;
        this.securityService = securityService;

        setSizeFull();

        configureGrid();

        Button addBtn = new Button("Add Approval");

        Button saveBtn = new Button("Submit");

        addBtn.addClickListener(event ->
                addLine());

        saveBtn.addClickListener(event ->
                saveApprovals());

        add(

                new H2("Purchase Order Approval Setup"),

                new HorizontalLayout(
                        addBtn,
                        saveBtn
                ),

                grid
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id =
                Long.parseLong(
                        event.getRouteParameters()
                                .get("id")
                                .get()
                );

        header =
                poService
                        .getPurchaseOrderHeaderById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Purchase Order Not Found"
                                )
                        );
    }

    // =========================================================
    // GRID
    // =========================================================

    private void configureGrid() {

        grid.removeAllColumns();

        grid.addColumn(
                AssigningApprovals::getLevel
        ).setHeader("Level");

        grid.addComponentColumn(a -> {

            ComboBox<Employee> box =
                    new ComboBox<>();

            box.setItems(
                    employeeService.getEmployees()
            );

            box.setItemLabelGenerator(
                    Employee::getEmployeeName
            );

            box.setWidthFull();

            box.setValue(
                    a.getApprover()
            );

            box.addValueChangeListener(event ->

                    a.setApprover(
                            event.getValue()
                    )
            );

            return box;

        }).setHeader("Approver");

        grid.addColumn(
                AssigningApprovals::getStatus
        ).setHeader("Status");

        grid.setWidthFull();

        grid.setHeightFull();

        grid.setItems(approvals);
    }

    // =========================================================
    // ADD
    // =========================================================

    private void addLine() {

        int nextLevel = approvals.stream()

                .map(AssigningApprovals::getLevel)

                .filter(Objects::nonNull)

                .max(Integer::compareTo)

                .orElse(0) + 1;

        AssigningApprovals a =
                new AssigningApprovals();

        a.setLevel(nextLevel);

        a.setStatus(
                Status.WAITING_APPROVAL
        );

        a.setApprovalType(
                ApprovalType.PURCHASE_ORDER_APPROVAL
        );

        a.setSource(
                ApprovalSource.MANUAL
        );

        approvals.add(a);

        grid.setItems(approvals);
    }

    // =========================================================
    // SAVE
    // =========================================================

    private void saveApprovals() {

        Set<Integer> levels =
                new HashSet<>();

        for (AssigningApprovals a : approvals) {

            if (a.getApprover() == null) {

                Notification.show( "Approver Missing" );

                return;
            }

            if (!levels.add(a.getLevel())) {

                Notification.show( "Duplicate Level");

                return;
            }
        }

        for (AssigningApprovals a : approvals) {

            a.setReferenceId(
                    header.getPurchaseOrderId()
            );

            a.setAssignedDate(
                    LocalDate.now()
            );

            a.setAssignedBy( securityService
                            .getLoggedInUser()
                            .getEmployee()
            );
                
            approvalsService.addApprovals(a,a.getAssignedBy());
        }

        header.setStatus(Status.WAITING_APPROVAL);
        header.setLevel(approvals.size());
        poService.updatePurchaseOrderHeader(header,securityService.getLoggedInUser().getEmployee());

        Notification.show("Purchase Order Submitted");

        getUI().ifPresent(ui ->

                ui.navigate(
                        "purchase-order"
                )
        );
    }
}