package com.module.purchase.view.purchaseRequest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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

    private final Grid<PurchaseRequestDTO> prGrid = new Grid<>(PurchaseRequestDTO.class, false);
    private final Grid<AssigningApprovalsDTO> assignGrid = new Grid<>(AssigningApprovalsDTO.class, false);

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private final Span pageInfo = new Span();

    private String viewMode = "CREATED"; 

    private PurchaseRequestDTO prFilter = new PurchaseRequestDTO();
    private AssigningApprovalsDTO assignFilter = new AssigningApprovalsDTO();

    private final TextField prIdField = new TextField("PR ID");
    private final ComboBox<Employee> createdByField = new ComboBox<>("Created By");
    private final ComboBox<Department> departmentField = new ComboBox<>("Department");
    private final ComboBox<Status> statusField = new ComboBox<>("Status");

    private final TextField assignIdField = new TextField("Assign ID");
    private final TextField referenceIdField = new TextField("Reference ID");
    private final ComboBox<Status> assignStatusField = new ComboBox<>("Status");

    private HorizontalLayout prFilters;
    private HorizontalLayout assignFilters;

    private final HorizontalLayout tabsContainer = new HorizontalLayout();
    private final Button allBtn = new Button("All Purchase Requests");
    private final Button assignedBtn = new Button("Assigned to You");
    private final Button createdBtn = new Button("Created by You");
    private final HorizontalLayout paginationLayout = new HorizontalLayout();

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

        assignFilter.setApprovalType(ApprovalType.PURCHASE_REQUEST);
        assignFilter.setStatus(Status.WAITING_APPROVAL);

        buildUI();
        determineDefaultViewModeAndTabVisibility();
        loadData();
    }

    private void buildUI() {
        H2 title = new H2("Purchase Requests");

        Button addButton = new Button("Add Purchase Request");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        addButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("purchase-request-form")));
        addButton.setVisible(securityService.canAccessView("purchase-request-form"));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

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
        allBtn.getStyle().setFontWeight(900);
        assignedBtn.getStyle().setFontWeight(900);
        createdBtn.getStyle().setFontWeight(900);

        tabsContainer.add(allBtn, assignedBtn, createdBtn);
        tabsContainer.setSpacing(true);

        prIdField.setPattern("[0-9]{0,20}");
        prIdField.setErrorMessage("Enter a valid nuumber");

        assignIdField.setPattern("[0-9]{0,20}");
        assignIdField.setErrorMessage("Enter a valid nuumber");

        referenceIdField.setPattern("[0-9]{0,20}");
        referenceIdField.setErrorMessage("Enter a valid nuumber");

        createdByField.setItems(employeeService.getEmployees());
        createdByField.setItemLabelGenerator(Employee::getEmployeeName);

        departmentField.setItems(departmentService.getDepartments());
        departmentField.setItemLabelGenerator(Department::getDepartmentName);

        statusField.setItems(Status.values());
        statusField.setItems(Status.WAITING_APPROVAL, Status.APPROVED, Status.REJECTED,Status.PARTIALLY_APPROVED,Status.CANCELLED);
        statusField.setItemLabelGenerator(Status::getDisplayName);

        Button search = new Button("Search", e -> applyFilter());
        search.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button clear = new Button("Clear", e -> clearFilter());
        clear.addThemeVariants(ButtonVariant.LUMO_ERROR);

        prFilters = new HorizontalLayout(prIdField, departmentField, createdByField, statusField, search, clear);
        prFilters.setAlignItems(Alignment.END);

        assignStatusField.setItems(Status.WAITING_APPROVAL, Status.APPROVED, Status.REJECTED,Status.PARTIALLY_APPROVED);
        assignStatusField.setItemLabelGenerator(Status::getDisplayName);
        assignStatusField.setValue(Status.WAITING_APPROVAL);

        Button assignSearch = new Button("Search", e -> applyAssignFilter());
        assignSearch.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button assignClear = new Button("Clear", e -> clearAssignFilter());
        assignClear.addThemeVariants(ButtonVariant.LUMO_ERROR);

        assignFilters = new HorizontalLayout(assignIdField, referenceIdField, assignStatusField, assignSearch, assignClear);
        assignFilters.setAlignItems(Alignment.END);

        prGrid.removeAllColumns();
        prGrid.addColumn(PurchaseRequestDTO::getPurchaseRequestId).setHeader("PR ID");
        prGrid.addColumn(pr -> pr.getForDepartment() == null ? "" : pr.getForDepartment().getDepartmentName()).setHeader("Department");
        prGrid.addColumn(pr -> pr.getCreatedBy() == null ? "" : pr.getCreatedBy().getEmployeeName()).setHeader("Created By");
        prGrid.addComponentColumn(pr -> {Span badge = new Span(pr.getStatus() != null ? pr.getStatus().name() : "UNKNOWN");
            badge.getStyle()
                 .set("padding", "2px 8px")
                 .set("border-radius", "4px")
                 .set("font-weight", "bold")
                 .set("font-size", "12px");

            if (pr.getStatus() == Status.DRAFT) {
                badge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
            } else if (pr.getStatus() == Status.APPROVED) {
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
            } else if (pr.getStatus() == Status.REJECTED) {
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
            }else if (pr.getStatus() == Status.CANCELLED) {
                badge.getStyle().set("background-color", "#f8e2fe").set("color", "#871cb9");
            } else {
                badge.getStyle().set("background-color", "#fef9c3").set("color", "#a16207");
            }
            return badge;}).setHeader("Status");

        prGrid.setWidthFull();
        prGrid.setHeightFull();
        prGrid.getStyle().set("border-radius", "12px").set("overflow", "hidden");
        prGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        prGrid.addItemDoubleClickListener(event -> {
            PurchaseRequestDTO pr = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("purchase-request-details/" + pr.getPurchaseRequestId()));
        });

        assignGrid.removeAllColumns();
        assignGrid.addColumn(AssigningApprovalsDTO::getAssigningApprovalsId).setHeader("Assign ID");
        assignGrid.addColumn(AssigningApprovalsDTO::getReferenceId).setHeader("Reference ID");
        assignGrid.addColumn(a -> a.getApprover() == null ? "" : a.getApprover().getEmployeeName()).setHeader("Approver");
        assignGrid.addColumn(AssigningApprovalsDTO::getLevel).setHeader("Level");
        assignGrid.addColumn(AssigningApprovalsDTO::getApprovalType).setHeader("Approval Type");
        assignGrid.addColumn(AssigningApprovalsDTO::getStatus).setHeader("Status");

        assignGrid.setWidthFull();
        assignGrid.setHeightFull();
        assignGrid.getStyle().set("border-radius", "12px").set("overflow", "hidden");
        assignGrid.addItemDoubleClickListener(event -> {
            AssigningApprovalsDTO a = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("assigned-approvals-details/" + a.getAssigningApprovalsId()));
        });

        Button prev = new Button("Previous", event -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });
        prev.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button next = new Button("Next", event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);
        pageSizeField.addValueChangeListener(event -> {
            pageSize = event.getValue();
            currentPage = 0;
            loadData();
        });

        paginationLayout.add(prev, pageInfo, next, new Span("Page Size"), pageSizeField);
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout, tabsContainer, prFilters, assignFilters, prGrid, assignGrid, paginationLayout);
        expand(prGrid);
        expand(assignGrid);
    }

    private void determineDefaultViewModeAndTabVisibility() {
        Users user = securityService.getLoggedInUser();
        Employee currentEmployee = user.getEmployee();
        
        List<EmployeeGroup> userGroups = (currentEmployee.getRole() != null) 
                ? currentEmployee.getRole().getEmployeeGroups()
                : List.of();

        boolean isManagementGroup = userGroups.stream().anyMatch(g -> 
                g == EmployeeGroup.SUPER_ADMIN || 
                g == EmployeeGroup.MANAGER || 
                g == EmployeeGroup.DIRECTOR || 
                g == EmployeeGroup.HR ||
                g == EmployeeGroup.PURCHASE
        );

        Page<PurchaseRequestDTO> createdPage = prService.getCreatedByUser(
                new PurchaseRequestDTO(), user.getUserId(), 0, 1);
        boolean hasCreatedRequests = createdPage.getTotalElements() > 0;
        
        boolean hasAssignedTasks = false;

            for (EmployeeGroup singleGroup : userGroups) {
                assignFilter.setEmployeeGroup(singleGroup);
                assignFilter.setStatus(null);
                if(assigningApprovalsService.getCountApprovals(assignFilter)>0){
                hasAssignedTasks = true;
                break;
                }

            } 
            assignFilter.setStatus(Status.WAITING_APPROVAL);
    
        if(userGroups.contains(EmployeeGroup.AUDITOR))
        {
            allBtn.setVisible(true);
            assignedBtn.setVisible(false); 
            createdBtn.setVisible(false);
            viewMode = "ALL";
        }  
        else if (isManagementGroup) {
            allBtn.setVisible(true);
            assignedBtn.setVisible(hasAssignedTasks); 
            createdBtn.setVisible(hasCreatedRequests);
            tabsContainer.setVisible(true);

            if (hasAssignedTasks) {
                viewMode = "ASSIGNED";
            } else {
                viewMode = "ALL";
            }
        } else {
            allBtn.setVisible(false);
            createdBtn.setVisible(hasCreatedRequests);
            assignedBtn.setVisible(hasAssignedTasks);

            if (!hasCreatedRequests && !hasAssignedTasks) {
                tabsContainer.setVisible(false); 
                viewMode = "NONE"; 
            } else {
                tabsContainer.setVisible(true);
                if (hasAssignedTasks) {
                    viewMode = "ASSIGNED";
                } else {
                    viewMode = "CREATED";
                }
            }
        }
    }

    private void updateButtonStyles() {
        allBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        assignedBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createdBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);

        switch (viewMode) {
            case "ALL" -> allBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            case "ASSIGNED" -> assignedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            case "CREATED" -> createdBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
    }

    private void loadData() {
        updateButtonStyles();

        Users user = securityService.getLoggedInUser();
        Long currentEmployeeId = user.getEmployee().getEmployeeId();

        prGrid.setVisible(false);
        assignGrid.setVisible(false);
        prFilters.setVisible(false);
        assignFilters.setVisible(false);
        paginationLayout.setVisible(true);

        if ("NONE".equals(viewMode)) {
            pageInfo.setText("Page 1 of 1");
            paginationLayout.setVisible(false);
            return;
        }

        if ("ASSIGNED".equals(viewMode)) {
            assignGrid.setVisible(true);
            assignFilters.setVisible(true);

            Employee currentEmployee = user.getEmployee();
            List<EmployeeGroup> userGroups = (currentEmployee.getRole() != null) 
                    ? currentEmployee.getRole().getEmployeeGroups() 
                    : List.of();

            List<EmployeeGroup> groupToQuery = new ArrayList<>(); 
            for (EmployeeGroup singleGroup : userGroups) {
                 assignFilter.setApprovalType(ApprovalType.PURCHASE_REQUEST);
                 assignFilter.setEmployeeGroup(singleGroup);
                if (assigningApprovalsService.getCountApprovals(assignFilter)>0) {
                    groupToQuery.add(singleGroup);
                }
            } 
            Page<AssigningApprovalsDTO> page = assigningApprovalsService.getPurchaseRequestApprovalsForMyGroup(
                    assignFilter, groupToQuery, currentPage, pageSize);

            assignGrid.setItems(page.getContent());
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        } else if ("CREATED".equals(viewMode)) {
            prGrid.setVisible(true);
            prFilters.setVisible(true);
            createdByField.setVisible(false);

            Page<PurchaseRequestDTO> page = prService.getCreatedByUser(
                    prFilter, user.getUserId(), currentPage, pageSize);

            prGrid.setItems(page.getContent());
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        } 
        
        else {
            prGrid.setVisible(true);
            prFilters.setVisible(true);
            createdByField.setVisible(true);
            Page<PurchaseRequestDTO> page = prService.getAllPurchaseRequest( prFilter, currentPage, pageSize);

            List<PurchaseRequestDTO> filteredContent = page.getContent().stream()
                    .filter(pr -> pr.getStatus() != Status.DRAFT || 
                             pr.getCreatedBy().getEmployeeId().equals(currentEmployeeId))
                    .toList();

            prGrid.setItems(filteredContent);
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        }
    }

    private void applyFilter() {
        prFilter = new PurchaseRequestDTO();
        if (!prIdField.isEmpty()) {
            prFilter.setPurchaseRequestId(Long.valueOf(prIdField.getValue().trim()));
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
        currentPage = 0;
        loadData();
    }

    private void applyAssignFilter() {
        assignFilter = new AssigningApprovalsDTO();
        if (!assignIdField.isEmpty()) {
            assignFilter.setAssigningApprovalsId(Long.valueOf(assignIdField.getValue().trim()));
        }
        if (!referenceIdField.isEmpty()) {
            assignFilter.setReferenceId(Long.valueOf(referenceIdField.getValue().trim()));
        }
        assignFilter.setStatus(assignStatusField.getValue());

        currentPage = 0;
        loadData();
    }

    private void clearAssignFilter() {
        assignIdField.clear();
        referenceIdField.clear();
        assignStatusField.setValue(Status.WAITING_APPROVAL);
      
        assignFilter = new AssigningApprovalsDTO();
         assignFilter.setApprovalType(ApprovalType.PURCHASE_REQUEST);
        assignFilter.setStatus(Status.WAITING_APPROVAL);
        currentPage = 0;
        loadData();
    }
}