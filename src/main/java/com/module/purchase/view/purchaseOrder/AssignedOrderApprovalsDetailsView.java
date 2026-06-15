package com.module.purchase.view.purchaseOrder;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.QuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "assigned-order-approvals-details/:id", layout = MainLayout.class)
@PermitAll
public class AssignedOrderApprovalsDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseOrderHeaderService poHeaderService;
    private final PurchaseOrderLineService poLineService;
    private final PurchaseRequestLineService prLineService;
    private final AssigningApprovalsService approvalsService;
    private final DepartmentBudgetService departmentBudgetService;
    private final SecurityService securityService;
    private final QuotationService quotationService;

    private PurchaseOrderHeader poHeader;
    private AssigningApprovals approvalTask;

    private final Span poIdText = new Span();
    private final Span vendorText = new Span();
    private final Span poTotalAmountText = new Span();
    private final Span workflowLevelText = new Span();

    private final VerticalLayout budgetTrackersContainer = new VerticalLayout();
    private final Grid<PurchaseOrderLine> poLinesGrid = new Grid<>(PurchaseOrderLine.class, false);

    private final Button approveBtn = new Button("Level");
    private final Button rejectBtn = new Button("Reject Order");
    private final TextArea commentsField = new TextArea("Approver Review Remarks / Annotations");

    private final List<PurchaseOrderLine> workingPoLinesList = new ArrayList<>();
    private boolean canUserActionApproval = false;
    private boolean isAnyDepartmentOverrun = false;

    public AssignedOrderApprovalsDetailsView(
            PurchaseOrderHeaderService poHeaderService,
            PurchaseOrderLineService poLineService,
            PurchaseRequestLineService prLineService,
            AssigningApprovalsService approvalsService,
            DepartmentBudgetService departmentBudgetService,
            SecurityService securityService,
            QuotationService quotationService) {

        this.poHeaderService = poHeaderService;
        this.poLineService = poLineService;
        this.prLineService = prLineService;
        this.approvalsService = approvalsService;
        this.departmentBudgetService = departmentBudgetService;
        this.securityService = securityService;
        this.quotationService = quotationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        commentsField.setWidthFull();
        commentsField.setMinHeight("100px");
        commentsField.setPlaceholder("Provide reasons for down-scaling quantities or rejections...");

        approveBtn.addThemeName("primary success");
        rejectBtn.addThemeName("primary error");

        approveBtn.addClickListener(e -> executeLevelApprovalTransaction());
        rejectBtn.addClickListener(e -> executeWorkflowRejectionTransaction());

        configurePoLinesGrid();

        budgetTrackersContainer.setPadding(false);
        budgetTrackersContainer.setSpacing(true);
        budgetTrackersContainer.setWidthFull();

        VerticalLayout headerCard = new VerticalLayout(poIdText, vendorText, poTotalAmountText, workflowLevelText);
        headerCard.setPadding(true);
        headerCard.setSpacing(false);
        headerCard.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "8px");

        HorizontalLayout actionToolbar = new HorizontalLayout(approveBtn, rejectBtn);
        actionToolbar.setSpacing(true);

        VerticalLayout layoutScrollerContent = new VerticalLayout(
                new H2("Purchase Order Approval"),
                headerCard,
                new Hr(),
                new H3("Cost Center Budgets"),
                budgetTrackersContainer,
                new Hr(),
                new H3("Purchase Order Lines"),
                poLinesGrid,
                commentsField,
                actionToolbar
        );
        layoutScrollerContent.setWidthFull();
        layoutScrollerContent.setPadding(false);

        Scroller viewScroller = new Scroller(layoutScrollerContent);
        viewScroller.setSizeFull();
        add(viewScroller);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long approvalId = Long.parseLong(event.getRouteParameters().get("id").get());

        approvalTask = approvalsService.getAssigningApprovalById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval task trace reference is missing."));

        poHeader = poHeaderService.getPurchaseOrderHeaderById(approvalTask.getReferenceId())
                .orElseThrow(() -> new RuntimeException("Linked parent Purchase Order file missing."));

        evaluateAccessPrivileges();
        bindHeaderMetadata();
        loadPoLinesDataset();
    }

    private void bindHeaderMetadata() {
        poIdText.setText("Purchase Order ID : PO-" + poHeader.getPurchaseOrderId());
        poIdText.getStyle().set("font-weight", "bold");
        vendorText.setText(" Vendor : " + (poHeader.getVendor() != null ? poHeader.getVendor().getVendorName() : "-"));
        workflowLevelText.setText("Approval Level" + approvalTask.getLevel());
    }

    private void evaluateAccessPrivileges() {
        Employee activeEmployeeProfile = securityService.getLoggedInUser().getEmployee();
        EmployeeGroup targetedGroup = approvalTask.getEmployeeGroup();
        List<EmployeeGroup> associatedUserGroups = activeEmployeeProfile.getRole().getEmployeeGroups();

        boolean isTaskPending = approvalTask.getStatus().equals(Status.WAITING_APPROVAL);
        this.canUserActionApproval = isTaskPending && isUserInAssignedGroup(associatedUserGroups, targetedGroup);

        if (canUserActionApproval) {
            approveBtn.setVisible(true);
            rejectBtn.setVisible(true);
            commentsField.setReadOnly(false);
            commentsField.setValue(approvalTask.getComments() == null ? "" : approvalTask.getComments());
        } else {
            approveBtn.setVisible(false);
            rejectBtn.setVisible(false);
            commentsField.setReadOnly(true);
            commentsField.setValue(approvalTask.getComments() == null ? "No review comments logged." : approvalTask.getComments());
        }
    }

    private boolean isUserInAssignedGroup(List<EmployeeGroup> userGroups, EmployeeGroup requiredGroup) {
        return userGroups.contains(requiredGroup) || userGroups.contains(EmployeeGroup.SUPER_ADMIN);
    }

    private void configurePoLinesGrid() {
        poLinesGrid.removeAllColumns();
        poLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item Name").setAutoWidth(true);

        poLinesGrid.addComponentColumn(line -> {
            NumberField quantityAdjustmentField = new NumberField();
            quantityAdjustmentField.setWidth("130px");
            quantityAdjustmentField.setValue(line.getQuantity());
            quantityAdjustmentField.setMin(0.0);
            quantityAdjustmentField.setStepButtonsVisible(true);
            quantityAdjustmentField.setReadOnly(!canUserActionApproval);

            quantityAdjustmentField.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    line.setQuantity(event.getValue());
                    executeCasadingSlabDiscountRecalculation(line);
                    refreshBudgetTrackersAnalysis();
                }
            });

            return quantityAdjustmentField;
        }).setHeader("Approved Quantity").setWidth("160px");

        poLinesGrid.addColumn(line -> String.format("%.2f INR", line.getUnitPrice())).setHeader("Unit Price").setWidth("130px");
        poLinesGrid.addColumn(line -> String.format("%.2f INR", line.getDiscountAmount())).setHeader("Slab Discount Amount").setWidth("150px");
        poLinesGrid.addColumn(line -> String.format("%.2f INR", line.getTotalAmount())).setHeader("Line Total").setWidth("150px");

        poLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        poLinesGrid.setAllRowsVisible(true);
    }

    private void loadPoLinesDataset() {
        workingPoLinesList.clear();
        List<PurchaseOrderLine> dbLines = poLineService.getPurchaseOrderLineByHeader(poHeader);
        workingPoLinesList.addAll(dbLines);
        poLinesGrid.setItems(workingPoLinesList);
        
        refreshBudgetTrackersAnalysis();
    }

    private void executeCasadingSlabDiscountRecalculation(PurchaseOrderLine poLine) {
        double currentQty = poLine.getQuantity();
        double activeDiscountPercent = 0.0;

        if (poHeader.getQuotation() != null) {
            List<QuotationLine> quotationLines = quotationService.getLinesByQuotation(poHeader.getQuotation());
            QuotationLine matchingQuoteLine = quotationLines.stream()
                    .filter(ql -> ql.getItemVariant().getId().equals(poLine.getItemVariant().getId()))
                    .findFirst().orElse(null);

            if (matchingQuoteLine != null && matchingQuoteLine.getDiscountTypes() != null) {
                for (DiscountType slab : matchingQuoteLine.getDiscountTypes()) {
                    boolean lowerBoundMatch = currentQty >= slab.getFromQuantity();
                    boolean upperBoundMatch = (slab.getToQuantity() == null) || (currentQty <= slab.getToQuantity());

                    if (lowerBoundMatch && upperBoundMatch) {
                        activeDiscountPercent = slab.getDiscountPercentage();
                        break;
                    }
                }
            }
        }

        double baseGrossCost = poLine.getUnitPrice() * currentQty;
        double netDiscountValue = baseGrossCost * (activeDiscountPercent / 100.0);
        
        poLine.setDiscountAmount(netDiscountValue);
        poLine.setTotalAmount(baseGrossCost - netDiscountValue);
        
        poLinesGrid.getDataProvider().refreshItem(poLine);
    }

    private void refreshBudgetTrackersAnalysis() {
        budgetTrackersContainer.removeAll();
        isAnyDepartmentOverrun = false;

        double cumulativePoNetCost = 0.0;
        Map<Department, Double> localizedCostMap = new HashMap<>();

        for (PurchaseOrderLine line : workingPoLinesList) {
            cumulativePoNetCost += line.getTotalAmount();
            if (line.getPurchaseRequestLines() != null && !line.getPurchaseRequestLines().isEmpty()) {
                double distributedCostSegment = line.getTotalAmount() / line.getPurchaseRequestLines().size();
                for (PurchaseRequestLine prl : line.getPurchaseRequestLines()) {
                    if (prl.getPurchaseRequestHeader() != null && prl.getPurchaseRequestHeader().getForDepartment() != null) {
                        Department d = prl.getPurchaseRequestHeader().getForDepartment();
                        localizedCostMap.put(d, localizedCostMap.getOrDefault(d, 0.0) + distributedCostSegment);
                    }
                }
            }
        }

        poTotalAmountText.setText("Evaluated Document Total: " + String.format("%.2f INR", cumulativePoNetCost));
        poTotalAmountText.getStyle().set("font-weight", "bold");

        for (Map.Entry<Department, Double> entry : localizedCostMap.entrySet()) {
            Department dept = entry.getKey();
            Double computedCost = entry.getValue();

            DepartmentBudget ledger = departmentBudgetService.getByDepartmentAndYear(dept, Year.now());
            HorizontalLayout statusRow = new HorizontalLayout();
            statusRow.setWidthFull();

            if (ledger != null) {
                double safeBalance = ledger.getRemainingBudgetAmount();
                Span infoLabel = new Span(String.format("Center: %s  |  Allocated PO Cost: %.2f INR  |  Available Budget: %.2f INR", 
                        dept.getDepartmentName(), computedCost, safeBalance));

                if (safeBalance < computedCost) {
                    isAnyDepartmentOverrun = true;
                    infoLabel.getStyle().set("color", "var(--lumo-error-text-color)").set("font-weight", "bold");
                    statusRow.add(VaadinIcon.WARNING.create(), infoLabel);
                    statusRow.getStyle().set("background-color", "#fef2f2").set("padding", "4px");
                } else {
                    infoLabel.getStyle().set("color", "var(--lumo-success-text-color)");
                    statusRow.add(VaadinIcon.CHECK.create(), infoLabel);
                    statusRow.getStyle().set("background-color", "#f0fdf4").set("padding", "4px");
                }
            }
            budgetTrackersContainer.add(statusRow);
        }

        approveBtn.setEnabled(!isAnyDepartmentOverrun && canUserActionApproval);
    }

    private void executeLevelApprovalTransaction() {
        if (isAnyDepartmentOverrun) {
            Notification.show("Compliance Fault: Action blocked. One or more cost centers exceed allocated budget caps.", 4000, Position.TOP_CENTER);
            return;
        }

        try {
            Employee actionActor = securityService.getLoggedInUser().getEmployee();

            for (PurchaseOrderLine line : workingPoLinesList) {
                poLineService.savePurchaseOrderLine(line);
                
                if (line.getPurchaseRequestLines() != null) {
                    for (PurchaseRequestLine prl : line.getPurchaseRequestLines()) {
                        prl.setOrderedQuantity(line.getQuantity());
                        prLineService.updatePurchaseRequestLine(prl, actionActor);
                    }
                }
            }

            List<AssigningApprovals> approvalChain = approvalsService.getAssigningApprovalByTypeAndReferId(
                    ApprovalType.PURCHASE_ORDER, 
                    poHeader.getPurchaseOrderId()
            );

            boolean isFinalTierStep = approvalTask.getLevel() == poHeader.getLevel();

            approvalTask.setStatus(Status.APPROVED);
            approvalTask.setComments(commentsField.getValue().trim());
            approvalTask.setApprovedDate(LocalDate.now());
            approvalTask.setApprover(actionActor);

            if (isFinalTierStep) {
                poHeader.setStatus(Status.APPROVED);
                poHeader.setTotalAmount(workingPoLinesList.stream().mapToDouble(PurchaseOrderLine::getTotalAmount).sum());
                poHeaderService.savePurchaseOrderHeader(poHeader);

                deductFinalizedDepartmentBudgets(workingPoLinesList);
                approvalsService.saveAssigningApproval(approvalTask);

                Notification.show("Final authorization secured. Purchase Order officially issued!", 4000, Position.TOP_CENTER);
            } else {
                if (poHeader.getLevel() > approvalTask.getLevel()) {
                    AssigningApprovals nextTier = approvalsService.getAssigningApprovalByTypeAndReferIdAndLevle(
                            ApprovalType.PURCHASE_ORDER,
                            poHeader.getPurchaseOrderId(),
                            approvalTask.getLevel() + 1
                    );
                    nextTier.setStatus(Status.WAITING_APPROVAL);
                    approvalsService.saveAssigningApproval(nextTier);
                }
                approvalsService.saveAssigningApproval(approvalTask);
                Notification.show("Level sign-off complete. Advanced to next workflow authority group tier.", 4000, Position.TOP_CENTER);
            }

            getUI().ifPresent(ui -> ui.navigate("purchase-order"));

        } catch (Exception ex) {
            Notification.show("Transaction failed: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void deductFinalizedDepartmentBudgets(List<PurchaseOrderLine> lines) {
        for (PurchaseOrderLine line : lines) {
            if (line.getPurchaseRequestLines() != null) {
                double splitCost = line.getTotalAmount() / line.getPurchaseRequestLines().size();
                for (PurchaseRequestLine prl : line.getPurchaseRequestLines()) {
                    Department d = prl.getPurchaseRequestHeader().getForDepartment();
                    DepartmentBudget ledger = departmentBudgetService.getByDepartmentAndYear(d, Year.now());
                    if (ledger != null) {
                        ledger.setRemainingBudgetAmount(ledger.getRemainingBudgetAmount() - splitCost);
                        departmentBudgetService.saveDepartmentBudget(ledger);
                    }
                }
            }
        }
    }

    private void executeWorkflowRejectionTransaction() {
        if (commentsField.isEmpty()) {
            Notification.show("Validation Fault: Rejection requires descriptive comments feedback context.", 3000, Position.TOP_CENTER);
            return;
        }

        try {
            Employee actionActor = securityService.getLoggedInUser().getEmployee();

            poHeader.setStatus(Status.REJECTED);
            poHeaderService.savePurchaseOrderHeader(poHeader);

            approvalTask.setStatus(Status.REJECTED);
            approvalTask.setComments(commentsField.getValue().trim());
            approvalTask.setApprovedDate(LocalDate.now());
            approvalTask.setApprover(actionActor);
            
            approvalsService.saveAssigningApproval(approvalTask);

            Notification.show("Purchase Order document rejected back to drafting state loops.", 3000, Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate("purchase-order"));

        } catch (Exception ex) {
            Notification.show("Rejection submission transaction failed: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }
}