package com.module.purchase.view.purchaseRequest;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.PurchaseRequestHeaderService;
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

@Route(value = "purchase-request", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestView extends VerticalLayout {

    private final PurchaseRequestHeaderService prService;
    private final AssigningApprovalsService assigningApprovalsService;
    private final SecurityService securityService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
//     private final ItemService itemService;
//     private final AssigningConfigService assigningConfigService;

    // ================= GRIDS =================

    private final Grid<PurchaseRequestDTO> prGrid =
            new Grid<>(PurchaseRequestDTO.class, false);

    private final Grid<AssigningApprovalsDTO> assignGrid =
            new Grid<>(AssigningApprovalsDTO.class, false);

    // ================= PAGINATION =================

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    // ================= VIEW MODE =================

    private String viewMode = "ALL";

    // ================= FILTER DTO =================

    private PurchaseRequestDTO prFilter =
            new PurchaseRequestDTO();

    private AssigningApprovalsDTO assignFilter =
            new AssigningApprovalsDTO();

    // ================= PR FILTERS =================

    private final TextField prIdField =
            new TextField("PR ID");

    private final ComboBox<Employee> createdByField =
            new ComboBox<>("Created By");

    private final ComboBox<Department> departmentField =
            new ComboBox<>("Department");

    private final ComboBox<Status> statusField =
            new ComboBox<>("Status");

    // ================= ASSIGN FILTERS =================

    private final TextField assignIdField =
            new TextField("Assign ID");

    private final TextField referenceIdField =
            new TextField("Reference ID");

    private final ComboBox<Status> assignStatusField =
            new ComboBox<>("Status");

    // ================= FILTER LAYOUTS =================

    private HorizontalLayout prFilters;
    private HorizontalLayout assignFilters;

    public PurchaseRequestView(

            PurchaseRequestHeaderService prService,
            SecurityService securityService,
            DepartmentService departmentService,
            EmployeeService employeeService,
            ItemService itemService,
            AssigningApprovalsService assigningApprovalsService,
            AssigningConfigService assigningConfigService) {

        this.prService = prService;
        this.securityService = securityService;
        this.departmentService = departmentService;
        this.employeeService = employeeService;
      //  this.itemService = itemService;
        this.assigningApprovalsService = assigningApprovalsService;
     //   this.assigningConfigService = assigningConfigService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        assignFilter.setStatus(Status.WAITING_APPROVAL);

        buildUI();
        loadData();
    }

    private void buildUI() {

        H2 title = new H2("Purchase Requests");

        Button addButton =
                new Button("Add Purchase Request");

        addButton.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate("purchase-request-form")));

        HorizontalLayout headerLayout =
                new HorizontalLayout(title, addButton);

        headerLayout.setWidthFull();

        headerLayout.setJustifyContentMode(
                JustifyContentMode.BETWEEN);

        headerLayout.setAlignItems(Alignment.CENTER);

        Button allBtn = new Button("Purchase Requests");

        Button assignedBtn = new Button("Assigned to You");

        Button createdBtn = new Button("Created by You");

        allBtn.addClickListener(event -> {

            viewMode = "ALL";
            currentPage = 0;

            loadData();
        });

        assignedBtn.addClickListener(event -> {

            viewMode = "ASSIGNED";
            currentPage = 0;

            loadData();
        });

        createdBtn.addClickListener(event -> {

            viewMode = "CREATED";
            currentPage = 0;

            loadData();
        });

        HorizontalLayout tabs =
                new HorizontalLayout(
                        allBtn,
                        assignedBtn,
                        createdBtn);

        createdByField.setItems(employeeService.getEmployees());

        createdByField.setItemLabelGenerator( Employee::getEmployeeName);

        departmentField.setItems(departmentService.getDepartments());

        departmentField.setItemLabelGenerator( Department::getDepartmentName);

        statusField.setItems(Status.values());


        Button search = new Button("Search",
                        e -> applyFilter());

        Button clear = new Button("Clear",
                        e -> clearFilter());

        prFilters = new HorizontalLayout(

                prIdField,
                createdByField,
                departmentField,
                statusField,
                search,
                clear);

        prFilters.setAlignItems(Alignment.END);

        assignStatusField.setItems(
                Status.WAITING_APPROVAL,
                Status.APPROVED,
                Status.REJECTED);

        assignStatusField.setValue(
                Status.WAITING_APPROVAL);

        Button assignSearch =
                new Button("Search",
                        e -> applyAssignFilter());

        Button assignClear =
                new Button("Clear",
                        e -> clearAssignFilter());

        assignFilters = new HorizontalLayout(

                assignIdField,
                referenceIdField,
                assignStatusField,
                assignSearch,
                assignClear);

        assignFilters.setAlignItems(Alignment.END);

        prGrid.removeAllColumns();

        prGrid.addComponentColumn(pr -> {

            Button button = new Button(
                    String.valueOf(
                            pr.getPurchaseRequestId()));

            button.addClickListener(event ->
                    getUI().ifPresent(ui ->
                            ui.navigate(
                                    "purchase-request-details/"
                                            + pr.getPurchaseRequestId())));

            return button;

        }).setHeader("PR ID");

        prGrid.addColumn(
                PurchaseRequestDTO::getStatus)
                .setHeader("Status");

        prGrid.addColumn(pr ->

                pr.getForDepartment() == null
                        ? ""
                        : pr.getForDepartment()
                        .getDepartmentName()

        ).setHeader("Department");

        prGrid.addColumn(pr ->

                pr.getCreatedBy() == null
                        ? ""
                        : pr.getCreatedBy()
                        .getEmployeeName()

        ).setHeader("Created By");

        prGrid.addColumn(
                PurchaseRequestDTO::getTotalAmount)
                .setHeader("Total Amount");

        prGrid.setWidthFull();
        prGrid.setHeightFull();
   
        assignGrid.removeAllColumns();

        assignGrid.addComponentColumn(a -> {

            Button button =
                    new Button(String.valueOf(
                            a.getAssigningApprovalsId()));

            button.addClickListener(event ->
                    getUI().ifPresent(ui ->
                            ui.navigate(
                                    "assigning-approvals-details/"
                                            + a.getAssigningApprovalsId())));

            return button;

        }).setHeader("Assign ID");

        assignGrid.addColumn(
                AssigningApprovalsDTO::getReferenceId)
                .setHeader("Reference ID");

        assignGrid.addColumn(a ->

                a.getApprover() == null
                        ? ""
                        : a.getApprover()
                        .getEmployeeName()

        ).setHeader("Approver");

        assignGrid.addColumn(
                AssigningApprovalsDTO::getLevel)
                .setHeader("Level");

        assignGrid.addColumn(
                AssigningApprovalsDTO::getApprovalType)
                .setHeader("Approval Type");

        assignGrid.addColumn(
                AssigningApprovalsDTO::getStatus)
                .setHeader("Status");

        assignGrid.setWidthFull();
        assignGrid.setHeightFull();

        // =====================================================
        // PAGINATION
        // =====================================================

        Button prev =
                new Button("Prev");

        Button next =
                new Button("Next");

        prev.addClickListener(event -> {

            if (currentPage > 0) {

                currentPage--;

                loadData();
            }
        });

        next.addClickListener(event -> {

            currentPage++;

            loadData();
        });

        ComboBox<Integer> pageSizeField =
                new ComboBox<>();

        pageSizeField.setItems(
                10,
                25,
                50,
                100);

        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(event -> {

            pageSize = event.getValue();

            currentPage = 0;

            loadData();
        });

        HorizontalLayout pagination =
                new HorizontalLayout(

                        prev,
                        pageInfo,
                        next,
                        new Span("Page Size"),
                        pageSizeField);

        pagination.setWidthFull();

        pagination.setJustifyContentMode( JustifyContentMode.CENTER);

        pagination.setAlignItems( Alignment.CENTER);

        // =====================================================
        // ADD COMPONENTS
        // =====================================================

        add(

                headerLayout,
                tabs,
                prFilters,
                assignFilters,
                prGrid,
                assignGrid,
                pagination);

        expand(prGrid);
        expand(assignGrid);
    }

    // =========================================================
    // LOAD DATA
    // =========================================================

    private void loadData() {

        Users user =
                securityService.getLoggedInUser();

        prGrid.setVisible(false);
        assignGrid.setVisible(false);

        prFilters.setVisible(false);
        assignFilters.setVisible(false);

        // ================= ASSIGNED =================

        if ("ASSIGNED".equals(viewMode)) {

            assignGrid.setVisible(true);
            assignFilters.setVisible(true);

            Page<AssigningApprovalsDTO> page =

                    assigningApprovalsService
                            .getPurchaseRequestApprovalsForMe(

                                    assignFilter,
                                    user.getUserId(),
                                    currentPage,
                                    pageSize);

            assignGrid.setItems(
                    page.getContent());

            pageInfo.setText(
                    "Page "
                            + (currentPage + 1)
                            + " of "
                            + page.getTotalPages());
        }

        // ================= CREATED =================

        else if ("CREATED".equals(viewMode)) {

            prGrid.setVisible(true);
            prFilters.setVisible(true);

            Page<PurchaseRequestDTO> page =

                    prService.getCreatedByUser(

                            prFilter,
                            user.getUserId(),
                            currentPage,
                            pageSize);

            prGrid.setItems(
                    page.getContent());

            pageInfo.setText(
                    "Page "
                            + (currentPage + 1)
                            + " of "
                            + page.getTotalPages());
        }

        // ================= ALL =================

        else {

            prGrid.setVisible(true);
            prFilters.setVisible(true);

            Page<PurchaseRequestDTO> page =

                    prService.getAllPurchaseRequest(

                            prFilter,
                            currentPage,
                            pageSize);

            prGrid.setItems(
                    page.getContent());

            pageInfo.setText(
                    "Page "
                            + (currentPage + 1)
                            + " of "
                            + page.getTotalPages());
        }
    }

    // =========================================================
    // PR FILTER
    // =========================================================

    private void applyFilter() {

        prFilter =
                new PurchaseRequestDTO();

        if (!prIdField.isEmpty()) {

            prFilter.setPurchaseRequestId(

                    Long.valueOf(
                            prIdField.getValue()));
        }

        prFilter.setCreatedBy(
                createdByField.getValue());

        prFilter.setForDepartment(
                departmentField.getValue());

        prFilter.setStatus(
                statusField.getValue());

        currentPage = 0;

        loadData();
    }

    private void clearFilter() {

        prIdField.clear();

        createdByField.clear();

        departmentField.clear();

        statusField.clear();

        prFilter =
                new PurchaseRequestDTO();

        currentPage = 0;

        loadData();
    }

    // =========================================================
    // ASSIGN FILTER
    // =========================================================

    private void applyAssignFilter() {

        assignFilter =
                new AssigningApprovalsDTO();

        if (!assignIdField.isEmpty()) {

            assignFilter.setAssigningApprovalsId(

                    Long.valueOf(
                            assignIdField.getValue()));
        }

        if (!referenceIdField.isEmpty()) {

            assignFilter.setReferenceId(

                    Long.valueOf(
                            referenceIdField.getValue()));
        }

        assignFilter.setStatus(
                assignStatusField.getValue());

        currentPage = 0;

        loadData();
    }

    private void clearAssignFilter() {

        assignIdField.clear();

        referenceIdField.clear();

        assignStatusField.setValue(
                Status.WAITING_APPROVAL);

        assignFilter =
                new AssigningApprovalsDTO();

        assignFilter.setStatus(
                Status.WAITING_APPROVAL);

        currentPage = 0;

        loadData();
    }
}