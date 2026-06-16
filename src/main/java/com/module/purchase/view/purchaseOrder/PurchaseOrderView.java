package com.module.purchase.view.purchaseOrder;

import java.util.List;
import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.PurchaseOrderHeaderService;
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

@Route(value = "purchase-order", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderView extends VerticalLayout {

    private final PurchaseOrderHeaderService poService;
    private final AssigningApprovalsService assigningApprovalsService;
    private final SecurityService securityService;
    private final EmployeeService employeeService;

    private final Grid<PurchaseOrderDTO> poGrid = new Grid<>(PurchaseOrderDTO.class, false);
    private final Grid<AssigningApprovalsDTO> assignGrid = new Grid<>(AssigningApprovalsDTO.class, false);

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;
    private final Span pageInfo = new Span();

    private String viewMode = "CREATED"; 

    private PurchaseOrderDTO poFilter = new PurchaseOrderDTO();
    private AssigningApprovalsDTO assignFilter = new AssigningApprovalsDTO();

    private final TextField poIdField = new TextField("PO ID");
    private final ComboBox<Employee> createdByField = new ComboBox<>("Created By");
    private final ComboBox<Status> statusField = new ComboBox<>("Status");

    private final TextField assignIdField = new TextField("Assign ID");
    private final TextField referenceIdField = new TextField("Reference ID");
    private final ComboBox<Status> assignStatusField = new ComboBox<>("Status");

    private HorizontalLayout poFilters;
    private HorizontalLayout assignFilters;

    private final HorizontalLayout tabsContainer = new HorizontalLayout();
    private final Button allBtn = new Button("All Purchase Orders");
    private final Button assignedBtn = new Button("Assigned to You");
    private final Button createdBtn = new Button("Generated Purchase Orders");
    private final HorizontalLayout paginationLayout = new HorizontalLayout();

    public PurchaseOrderView(
            PurchaseOrderHeaderService poService,
            SecurityService securityService,
            EmployeeService employeeService,
            AssigningApprovalsService assigningApprovalsService) {

        this.poService = poService;
        this.securityService = securityService;
        this.employeeService = employeeService;
        this.assigningApprovalsService = assigningApprovalsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        assignFilter.setStatus(Status.WAITING_APPROVAL);

        buildUI();
        determineDefaultViewModeAndTabVisibility();
        loadData();
    }

    private void buildUI() {
        H2 title = new H2("Purchase Orders");

        HorizontalLayout headerLayout = new HorizontalLayout(title);
        headerLayout.setWidthFull();
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

        tabsContainer.add(allBtn, assignedBtn, createdBtn);
        tabsContainer.setSpacing(true);

        createdByField.setItems(employeeService.getEmployees());
        createdByField.setItemLabelGenerator(Employee::getEmployeeName);

        statusField.setItems(Status.values());

        Button search = new Button("Search", e -> applyFilter());
        Button clear = new Button("Clear", e -> clearFilter());

        poFilters = new HorizontalLayout(poIdField, createdByField, statusField, search, clear);
        poFilters.setAlignItems(Alignment.END);

        assignStatusField.setItems(Status.WAITING_APPROVAL, Status.APPROVED, Status.REJECTED);
        assignStatusField.setValue(Status.WAITING_APPROVAL);

        Button assignSearch = new Button("Search", e -> applyAssignFilter());
        Button assignClear = new Button("Clear", e -> clearAssignFilter());

        assignFilters = new HorizontalLayout(assignIdField, referenceIdField, assignStatusField, assignSearch, assignClear);
        assignFilters.setAlignItems(Alignment.END);

        poGrid.removeAllColumns();
        poGrid.addColumn(PurchaseOrderDTO::getPurchaseOrderId).setHeader("PO ID");
        poGrid.addColumn(po -> po.getCreatedBy() == null ? "" : po.getCreatedBy().getEmployeeName()).setHeader("Created By");
        poGrid.addColumn(po -> po.getTotalAmount() != null ? String.format("%.2f INR", po.getTotalAmount()) : "0.00 INR").setHeader("Total Amount");
        poGrid.addColumn(po -> po.getStatus() != null ? po.getStatus().name() : "").setHeader("Status");

        poGrid.setWidthFull();
        poGrid.setHeightFull();
        poGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        
        poGrid.addItemDoubleClickListener(event -> {
            PurchaseOrderDTO po = event.getItem();
            if (po.getStatus() == Status.DRAFT) {
                getUI().ifPresent(ui -> ui.navigate("purchase-order-approval/" + po.getPurchaseOrderId()));
            } else {
                getUI().ifPresent(ui -> ui.navigate("purchase-order-details/" + po.getPurchaseOrderId()));
            }
        });

        assignGrid.removeAllColumns();
        assignGrid.addColumn(AssigningApprovalsDTO::getAssigningApprovalsId).setHeader("Assign ID");
        assignGrid.addColumn(AssigningApprovalsDTO::getReferenceId).setHeader("PO Reference ID");
        assignGrid.addColumn(a -> a.getApprover() == null ? "" : a.getApprover().getEmployeeName()).setHeader("Approver");
        assignGrid.addColumn(AssigningApprovalsDTO::getLevel).setHeader("Approval Level");
        assignGrid.addColumn(AssigningApprovalsDTO::getStatus).setHeader("Status");

        assignGrid.setWidthFull();
        assignGrid.setHeightFull();
        assignGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        
        assignGrid.addItemDoubleClickListener(event -> {
            AssigningApprovalsDTO a = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("assigned-order-approvals-details/" + a.getAssigningApprovalsId()));
        });

        Button prev = new Button("Prev", event -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });

        Button next = new Button("Next", event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);
        pageSizeField.setWidth("100px");
        pageSizeField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                pageSize = event.getValue();
                currentPage = 0;
                loadData();
            }
        });

        paginationLayout.add(prev, pageInfo, next, new Span("Page Size"), pageSizeField);
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        add(headerLayout, tabsContainer, poFilters, assignFilters, poGrid, assignGrid, paginationLayout);
        expand(poGrid);
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

        PurchaseOrderDTO initCheckFilter = new PurchaseOrderDTO();
        initCheckFilter.setStatus(Status.DRAFT);
        Page<PurchaseOrderDTO> createdPage = poService.getAllPurchaseOrder(initCheckFilter, 0, 1);
        boolean hasCreatedRequests = createdPage.getTotalElements() > 0;

        boolean hasAssignedTasks = false;
        for (EmployeeGroup singleGroup : userGroups) {
            Page<AssigningApprovalsDTO> assignedPage = assigningApprovalsService.getPurchaseRequestApprovalsForMyGroup(
                    new AssigningApprovalsDTO(), singleGroup, 0, 1); 
            if (assignedPage.getTotalElements() > 0) {
                hasAssignedTasks = true;
                break; 
            }
        }

        if (isManagementGroup) {
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

        poGrid.setVisible(false);
        assignGrid.setVisible(false);
        poFilters.setVisible(false);
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

            EmployeeGroup groupToQuery = EmployeeGroup.MANAGER; 
            for (EmployeeGroup singleGroup : userGroups) {
                assignFilter.setApprovalType(ApprovalType.PURCHASE_ORDER);
                Page<AssigningApprovalsDTO> testPage = assigningApprovalsService.getPurchaseRequestApprovalsForMyGroup(
                        assignFilter, singleGroup, currentPage, pageSize);
                if (testPage.getTotalElements() > 0) {
                    groupToQuery = singleGroup;
                    break;
                }
            }

            Page<AssigningApprovalsDTO> page = assigningApprovalsService.getPurchaseRequestApprovalsForMyGroup(
                    assignFilter, groupToQuery, currentPage, pageSize);

            assignGrid.setItems(page.getContent());
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
            
        } else if ("CREATED".equals(viewMode)) {
            poGrid.setVisible(true);
            poFilters.setVisible(true);
            
            createdByField.setVisible(false);
            statusField.setVisible(false);

            poFilter.setStatus(Status.DRAFT);

            Page<PurchaseOrderDTO> page = poService.getAllPurchaseOrder(poFilter, currentPage, pageSize);

            List<PurchaseOrderDTO> selfDrafts = page.getContent().stream()
                    .filter(po -> po.getCreatedBy() != null && po.getCreatedBy().getEmployeeId().equals(currentEmployeeId))
                    .toList();

            poGrid.setItems(selfDrafts);
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
            
        } else {
            poGrid.setVisible(true);
            poFilters.setVisible(true);
            
            createdByField.setVisible(true);
            statusField.setVisible(true);

            Page<PurchaseOrderDTO> page = poService.getAllPurchaseOrder(poFilter, currentPage, pageSize);

            List<PurchaseOrderDTO> filteredContent = page.getContent().stream()
                    .filter(po -> po.getStatus() != Status.DRAFT || 
                                 (po.getCreatedBy() != null && po.getCreatedBy().getEmployeeId().equals(currentEmployeeId)))
                    .toList();

            poGrid.setItems(filteredContent);
            this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;
            pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
        }
    }

    private void applyFilter() {
        poFilter = new PurchaseOrderDTO();
        if (!poIdField.isEmpty()) {
            poFilter.setPurchaseOrderId(Long.valueOf(poIdField.getValue().trim()));
        }
        
        if ("CREATED".equals(viewMode)) {
            poFilter.setStatus(Status.DRAFT);
        } else {
            poFilter.setCreatedBy(createdByField.getValue());
            poFilter.setStatus(statusField.getValue());
        }

        currentPage = 0;
        loadData();
    }

    private void clearFilter() {
        poIdField.clear();
        createdByField.clear();
        statusField.clear();

        poFilter = new PurchaseOrderDTO();
        if ("CREATED".equals(viewMode)) {
            poFilter.setStatus(Status.DRAFT);
        }
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
        assignFilter.setApprovalType(ApprovalType.PURCHASE_ORDER);
        assignFilter = new AssigningApprovalsDTO();
        assignFilter.setStatus(Status.WAITING_APPROVAL);
        currentPage = 0;
        loadData();
    }
}