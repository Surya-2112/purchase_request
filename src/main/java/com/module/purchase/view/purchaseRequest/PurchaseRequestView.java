package com.module.purchase.view.purchaseRequest;

import com.module.purchase.entity.Users;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Department;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.*;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.data.domain.Page;


@Route(value = "purchase-request", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestView extends VerticalLayout {

    private final PurchaseRequestHeaderService prService;
    private final AssigningApprovalsService assigningApprovalsService;
    private final SecurityService securityService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    private final Grid<PurchaseRequestDTO> prGrid = new Grid<>(PurchaseRequestDTO.class, false);
    private final Grid<AssigningApprovalsDTO> assignGrid = new Grid<>(AssigningApprovalsDTO.class, false);

    private int currentPage = 0;
    private int pageSize = 25;

    private String viewMode = "ALL";

    private PurchaseRequestDTO prFilter = new PurchaseRequestDTO();
    private AssigningApprovalsDTO assignFilter = new AssigningApprovalsDTO();

    private final Span pageInfo = new Span();

    private final TextField prIdField = new TextField("PR ID");
    private final ComboBox<Employee> createdByField = new ComboBox<>("Created By");
    private final ComboBox<Department> departmentField = new ComboBox<>("Department");
    private final ComboBox<Status> statusField = new ComboBox<>("Status");

    private final TextField assignIdField = new TextField("Assign ID");
    private final TextField referenceIdField = new TextField("Reference ID");

    public PurchaseRequestView(
            PurchaseRequestHeaderService prService,
            SecurityService securityService,
            DepartmentService departmentService,
            EmployeeService employeeService,
            AssigningApprovalsService assigningApprovalsService) {

        this.prService = prService;
        this.securityService = securityService;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
        this.assigningApprovalsService = assigningApprovalsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        buildUI();
        loadData();
    }

    private void buildUI() {

        H2 title = new H2("Purchase Requests");

        Button allBtn = new Button("Purchase Requests");
        Button assignedBtn = new Button("Assigned to You");
        Button createdBtn = new Button("Created by You");

        HorizontalLayout tabs = new HorizontalLayout(allBtn, assignedBtn, createdBtn);

        allBtn.addClickListener(e -> {
            viewMode = "ALL";
            currentPage = 0;
            loadData();
        });
        assignedBtn.addClickListener(e -> {
            viewMode = "ASSIGNED";
            currentPage = 0;
            loadData();
        });
        createdBtn.addClickListener(e -> {
            viewMode = "CREATED";
            currentPage = 0;
            loadData();
        });

        createdByField.setItems(employeeService.getEmployees());
        departmentField.setItems(departmentService.getDepartments());
        statusField.setItems(Status.values());

        Button search = new Button("Search", e -> applyFilter());
        Button clear = new Button("Clear", e -> clearFilter());

        HorizontalLayout prFilters = new HorizontalLayout(
                prIdField, createdByField, departmentField, statusField, search, clear);

        Button assignSearch = new Button("Search", e -> applyAssignFilter());
        Button assignClear = new Button("Clear", e -> clearAssignFilter());

        HorizontalLayout assignFilters = new HorizontalLayout(
                assignIdField, referenceIdField, assignSearch, assignClear);

        // ===== PR GRID =====
        prGrid.addComponentColumn(pr -> {
            Button id = new Button(String.valueOf(pr.getPurchaseRequestId()));
            id.addClickListener(
                    e -> getUI().ifPresent(ui -> ui.navigate("purchase-request-details/" + pr.getPurchaseRequestId())));
            return id;
        }).setHeader("PR ID");

        prGrid.addColumn(PurchaseRequestDTO::getStatus).setHeader("Status");

        prGrid.addColumn(pr -> pr.getForDepartment() == null ? "" : pr.getForDepartment().getDepartmentName())
                .setHeader("Department");

        prGrid.addColumn(pr -> pr.getCreatedBy() == null ? "" : pr.getCreatedBy().getEmployeeName())
                .setHeader("Created By");

        prGrid.setSizeFull();

        // ===== ASSIGN GRID =====
        assignGrid.addComponentColumn(a -> {
            Button id = new Button(String.valueOf(a.getAssigningApprovalsId()));
            id.addClickListener(e -> getUI()
                    .ifPresent(ui -> ui.navigate("assigning-approvals-details/" + a.getAssigningApprovalsId())));
            return id;
        }).setHeader("Assign ID");

        assignGrid.addColumn(AssigningApprovalsDTO::getReferenceId).setHeader("Reference ID");

        assignGrid.addColumn(a -> a.getApprover() == null ? "" : a.getApprover().getEmployeeName())
                .setHeader("Approver");

        assignGrid.setSizeFull();

        // ===== PAGINATION =====
        Button prev = new Button("Prev", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });

        Button next = new Button("Next", e -> {
            currentPage++;
            loadData();
        });

        HorizontalLayout pagination = new HorizontalLayout(prev, pageInfo, next);
        pagination.setWidthFull();
        pagination.setJustifyContentMode(JustifyContentMode.CENTER);

        add(title, tabs, prFilters, assignFilters, prGrid, assignGrid, pagination);

        expand(prGrid);
        expand(assignGrid);
    }

    // ================= LOAD DATA =================
    private void loadData() {

        Users user = securityService.getLoggedInUser();

        prGrid.setVisible(false);
        assignGrid.setVisible(false);

        if ("ASSIGNED".equals(viewMode)) {

            assignGrid.setVisible(true);

            Page<AssigningApprovalsDTO> page = assigningApprovalsService.getPurchaseRequestApprovalsForMe(
                    assignFilter,
                    user.getUserId(),
                    currentPage,
                    pageSize);

            assignGrid.setItems(page.getContent());

        } else if ("CREATED".equals(viewMode)) {
            prGrid.setVisible(true);
            Page<PurchaseRequestDTO> page = prService.getCreatedByUser(
                    prFilter,
                    user.getUserId(),
                    currentPage,
                    pageSize);
            prGrid.setItems(page.getContent());

        } else {

            prGrid.setVisible(true);

            Page<PurchaseRequestDTO> page = prService.getAllPurchaseRequest(prFilter, currentPage, pageSize);

            prGrid.setItems(page.getContent());
        }

        pageInfo.setText("Page " + (currentPage + 1));
    }

    // ================= FILTERS =================
    private void applyFilter() {

        prFilter = new PurchaseRequestDTO();

        if (!prIdField.isEmpty()) {
            prFilter.setPurchaseRequestId(Long.valueOf(prIdField.getValue()));
        }

        prFilter.setCreatedBy(createdByField.getValue());
        prFilter.setForDepartment(departmentField.getValue());
        prFilter.setStatus(statusField.getValue());

        currentPage = 0;
        loadData();
    }

    private void clearFilter() {
        prIdField.clear();
        createdByField.clear();
        departmentField.clear();
        statusField.clear();
        prFilter = new PurchaseRequestDTO();
        loadData();
    }

    private void applyAssignFilter() {

        assignFilter = new AssigningApprovalsDTO();

        if (!assignIdField.isEmpty()) {
            assignFilter.setAssigningApprovalsId(Long.valueOf(assignIdField.getValue()));
        }

        if (!referenceIdField.isEmpty()) {
            assignFilter.setReferenceId(Long.valueOf(referenceIdField.getValue()));
        }

        currentPage = 0;
        loadData();
    }

    private void clearAssignFilter() {
        assignIdField.clear();
        referenceIdField.clear();
        assignFilter = new AssigningApprovalsDTO();
        loadData();
    }
}