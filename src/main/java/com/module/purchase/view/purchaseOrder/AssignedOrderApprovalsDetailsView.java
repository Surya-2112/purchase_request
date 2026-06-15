
package com.module.purchase.view.purchaseOrder;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    // Binds the grid straight to your standard Department entity model
    private final Grid<Department> budgetGrid = new Grid<>();
    private final Grid<PurchaseOrderLine> poLinesGrid = new Grid<>(PurchaseOrderLine.class, false);
    private final Grid<PurchaseRequestLine> prLinesGrid = new Grid<>(PurchaseRequestLine.class, false);

    private final Button approveBtn = new Button("Authorize Level", VaadinIcon.CHECK_CIRCLE.create());
    private final Button rejectBtn = new Button("Reject Order", VaadinIcon.CLOSE_CIRCLE.create());
    private final TextArea commentsField = new TextArea("Approver Review Remarks");

    private final List<PurchaseOrderLine> workingPoLinesList = new ArrayList<>();
    private final List<PurchaseRequestLine> workingPrLinesList = new ArrayList<>();
    private final List<Department> workingBudgetList = new ArrayList<>();

    // Keep instance level references for live dynamic column rendering lookups
    private final Map<Long, Double> costAllocationMap = new HashMap<>();

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

        configureBudgetGrid();
        configurePoLinesGrid();
        configurePrLinesGrid();

        VerticalLayout headerCard = new VerticalLayout(poIdText, vendorText, poTotalAmountText, workflowLevelText);
        headerCard.setPadding(true);
        headerCard.setSpacing(false);
        headerCard.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "8px");

        HorizontalLayout actionToolbar = new HorizontalLayout(approveBtn, rejectBtn);
        actionToolbar.setSpacing(true);

        VerticalLayout layoutScrollerContent = new VerticalLayout(
                new H2("Purchase Order Approval Workspace"),
                headerCard,
                new Hr(),
                new H3("1. Cost Center Budgets Ledger Analysis (PO Linked Pricing)"),
                budgetGrid,
                new Hr(),
                new H3("2. Consolidated Purchase Order Lines Summary"),
                poLinesGrid,
                new Hr(),
                new H3("3. Originating Purchase Request Source Line Items"),
                prLinesGrid,
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
        loadMasterDataPipelines();
    }

    private void bindHeaderMetadata() {
        poIdText.setText("Purchase Order ID : PO-" + poHeader.getPurchaseOrderId());
        poIdText.getStyle().set("font-weight", "bold");
        vendorText.setText("Contracted Winning Vendor : " + (poHeader.getVendor() != null ? poHeader.getVendor().getVendorName() : "-"));
        workflowLevelText.setText("Active Authorization Step : Tier " + approvalTask.getLevel());
    }

    private void evaluateAccessPrivileges() {
        Employee activeEmployeeProfile = securityService.getLoggedInUser().getEmployee();
        EmployeeGroup targetedGroup = approvalTask.getEmployeeGroup();
        List<EmployeeGroup> associatedUserGroups = activeEmployeeProfile.getRole().getEmployeeGroups();

        boolean isTaskPending = approvalTask.getStatus().equals(Status.WAITING_APPROVAL);
        this.canUserActionApproval = isTaskPending && (associatedUserGroups.contains(targetedGroup) || associatedUserGroups.contains(EmployeeGroup.SUPER_ADMIN));

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

    private void configureBudgetGrid() {
        budgetGrid.removeAllColumns();
        
        budgetGrid.addColumn(Department::getDepartmentName).setHeader("Cost Center Department").setAutoWidth(true);
        
        budgetGrid.addColumn(dept -> {
            DepartmentBudget accountLedger = departmentBudgetService.getByDepartmentAndYear(dept, Year.now());
            double available = (accountLedger != null && accountLedger.getRemainingBudgetAmount() != null) ? accountLedger.getRemainingBudgetAmount() : 0.0;
            return String.format("%.2f INR", available);
        }).setHeader("Available Budget").setAutoWidth(true);
        
        budgetGrid.addColumn(dept -> {
            double needed = costAllocationMap.getOrDefault(dept.getDepartmentId(), 0.0);
            return String.format("%.2f INR", needed);
        }).setHeader("Needed Amount (PO Pricing)").setAutoWidth(true);
        
        budgetGrid.addColumn(dept -> {
            DepartmentBudget accountLedger = departmentBudgetService.getByDepartmentAndYear(dept, Year.now());
            double available = (accountLedger != null && accountLedger.getRemainingBudgetAmount() != null) ? accountLedger.getRemainingBudgetAmount() : 0.0;
            double needed = costAllocationMap.getOrDefault(dept.getDepartmentId(), 0.0);
            return String.format("%.2f INR", available - needed);
        }).setHeader("Remaining Balance").setAutoWidth(true);

        budgetGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        budgetGrid.setAllRowsVisible(true);

        // Grid Style Class Assignment via direct state verification
        budgetGrid.setClassNameGenerator(dept -> {
            DepartmentBudget accountLedger = departmentBudgetService.getByDepartmentAndYear(dept, Year.now());
            double available = (accountLedger != null && accountLedger.getRemainingBudgetAmount() != null) ? accountLedger.getRemainingBudgetAmount() : 0.0;
            double needed = costAllocationMap.getOrDefault(dept.getDepartmentId(), 0.0);
            return (available - needed < 0 || accountLedger == null) ? "error-budget-row" : "";
        });

        budgetGrid.getElement().executeJs(
            "const style = document.createElement('style');" +
            "style.innerHTML = '.error-budget-row { background-color: #fee2e2 !important; color: #b91c1c !important; font-weight: bold; }';" +
            "document.head.appendChild(style);"
        );
    }

    private void configurePoLinesGrid() {
        poLinesGrid.removeAllColumns();
        poLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item Name").setAutoWidth(true);
        poLinesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "").setHeader("Item Variant").setAutoWidth(true);
        poLinesGrid.addColumn(PurchaseOrderLine::getQuantity).setHeader("Quantity").setWidth("110px");
        poLinesGrid.addColumn(line -> String.format("%.2f INR", line.getUnitPrice() != null ? line.getUnitPrice() : 0.0)).setHeader("Unit Price").setWidth("130px");
        poLinesGrid.addColumn(line -> String.format("%.2f INR", line.getDiscountAmount() != null ? line.getDiscountAmount() : 0.0)).setHeader("Slab Discount").setWidth("150px");
        poLinesGrid.addColumn(line -> String.format("%.2f INR", line.getTotalAmount() != null ? line.getTotalAmount() : 0.0)).setHeader("Line Total").setWidth("150px");

        poLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        poLinesGrid.setAllRowsVisible(true);
    }

    private void configurePrLinesGrid() {
        prLinesGrid.removeAllColumns();
        prLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item Name").setAutoWidth(true);
        prLinesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "").setHeader("Variation").setAutoWidth(true);
        prLinesGrid.addColumn(line -> line.getPurchaseRequestHeader() != null && line.getPurchaseRequestHeader().getForDepartment() != null
                ? line.getPurchaseRequestHeader().getForDepartment().getDepartmentName() : "-").setHeader("Department Name").setAutoWidth(true);
        prLinesGrid.addColumn(PurchaseRequestLine::getApprovedQuantity).setHeader("Approved Qty").setWidth("110px");

        prLinesGrid.addComponentColumn(prLine -> {
            NumberField orderQtyField = new NumberField();
            orderQtyField.setWidth("130px");
            
            if (prLine.getOrderedQuantity() == null) {
                prLine.setOrderedQuantity(prLine.getApprovedQuantity());
            }
            
            orderQtyField.setValue(prLine.getOrderedQuantity());
            orderQtyField.setMin(0.0);
            double maxAllowed = prLine.getApprovedQuantity() != null ? prLine.getApprovedQuantity() : 0.0;
            orderQtyField.setMax(maxAllowed);
            orderQtyField.setStepButtonsVisible(true);
            orderQtyField.setReadOnly(!canUserActionApproval);

            orderQtyField.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    if (event.getValue() > maxAllowed) {
                        Notification.show("Ordered quantity cannot exceed approved limits.", 3000, Position.MIDDLE);
                        orderQtyField.setValue(maxAllowed);
                        prLine.setOrderedQuantity(maxAllowed);
                    } else {
                        prLine.setOrderedQuantity(event.getValue());
                    }
                    synchronizeMasterPurchaseOrderLinesFromPrChanges(prLine);
                }
            });

            return orderQtyField;
        }).setHeader("Order Qty (Edit)").setWidth("160px");

        prLinesGrid.addColumn(line -> line.getPurchaseOrderLine() != null && line.getPurchaseOrderLine().getUnitPrice() != null
                ? String.format("%.2f INR", line.getPurchaseOrderLine().getUnitPrice()) 
                : "0.00 INR").setHeader("Contract Unit Price").setWidth("140px");

        prLinesGrid.addColumn(line -> {
            double finalPrice = (line.getPurchaseOrderLine() != null && line.getPurchaseOrderLine().getUnitPrice() != null) 
                    ? line.getPurchaseOrderLine().getUnitPrice() : 0.0;
            double currentQty = line.getOrderedQuantity() != null ? line.getOrderedQuantity() : 0.0;
            return String.format("%.2f INR", finalPrice * currentQty);
        }).setHeader("Gross Total").setWidth("140px");

        prLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        prLinesGrid.setAllRowsVisible(true);
    }

    private void loadMasterDataPipelines() {
        workingPoLinesList.clear();
        workingPrLinesList.clear();

        List<PurchaseOrderLine> poLines = poLineService.getPurchaseOrderLineByHeader(poHeader);
        workingPoLinesList.addAll(poLines);
        poLinesGrid.setItems(workingPoLinesList);

        for (PurchaseOrderLine poLine : poLines) {
            List<PurchaseRequestLine> connectedPrLines = prLineService.getPurchaseRequestLineByOrder(poLine);
            for (PurchaseRequestLine prLine : connectedPrLines) {
                if (prLine.getOrderedQuantity() == null) {
                    prLine.setOrderedQuantity(prLine.getApprovedQuantity());
                }
            }
            workingPrLinesList.addAll(connectedPrLines);
        }
        prLinesGrid.setItems(workingPrLinesList);

        refreshBudgetTrackersAnalysis();
    }

    private void synchronizeMasterPurchaseOrderLinesFromPrChanges(PurchaseRequestLine changedPrLine) {
        if (changedPrLine.getPurchaseOrderLine() == null) return;
        
        Long targetPoLineId = changedPrLine.getPurchaseOrderLine().getId();

        PurchaseOrderLine parentPoLine = null;
        for (PurchaseOrderLine line : workingPoLinesList) {
            if (Objects.equals(line.getId(), targetPoLineId)) {
                parentPoLine = line;
                break;
            }
        }

        if (parentPoLine == null) return;

        double directAggregatedQuantity = 0.0;
        for (PurchaseRequestLine prl : workingPrLinesList) {
            if (prl.getPurchaseOrderLine() != null && Objects.equals(prl.getPurchaseOrderLine().getId(), targetPoLineId)) {
                directAggregatedQuantity += prl.getOrderedQuantity() != null ? prl.getOrderedQuantity() : 0.0;
            }
        }

        parentPoLine.setQuantity(directAggregatedQuantity);

        double appropriateSlabDiscount = 0.0;
        if (poHeader.getQuotation() != null) {
            List<QuotationLine> catalogRules = quotationService.getLinesByQuotation(poHeader.getQuotation());
            QuotationLine matchRuleRow = null;
            for (QuotationLine ql : catalogRules) {
                if (ql.getItemVariant() != null && parentPoLine.getItemVariant() != null 
                        && Objects.equals(ql.getItemVariant().getId(), parentPoLine.getItemVariant().getId())) {
                    matchRuleRow = ql;
                    break;
                }
            }

            if (matchRuleRow != null && matchRuleRow.getDiscountTypes() != null) {
                for (DiscountType slab : matchRuleRow.getDiscountTypes()) {
                    double fromQty = slab.getFromQuantity() != null ? slab.getFromQuantity() : 0.0;
                    Double toQtyObj = slab.getToQuantity();

                    boolean lowBound = directAggregatedQuantity >= fromQty;
                    boolean upBound = (toQtyObj == null) || (directAggregatedQuantity <= toQtyObj);

                    if (lowBound && upBound) {
                        appropriateSlabDiscount = slab.getDiscountPercentage() != null ? slab.getDiscountPercentage() : 0.0;
                        break;
                    }
                }
            }
        }

        double baseUnitPrice = parentPoLine.getUnitPrice() != null ? parentPoLine.getUnitPrice() : 0.0;
        double calculatedGrossCost = baseUnitPrice * directAggregatedQuantity;
        double netDiscountDeduction = calculatedGrossCost * (appropriateSlabDiscount / 100.0);

        parentPoLine.setDiscountAmount(netDiscountDeduction);
        parentPoLine.setTotalAmount(calculatedGrossCost - netDiscountDeduction);

        poLinesGrid.getDataProvider().refreshItem(parentPoLine);
        poLinesGrid.getDataProvider().refreshAll();
        prLinesGrid.getDataProvider().refreshAll();
        
        refreshBudgetTrackersAnalysis();
    }

    private void refreshBudgetTrackersAnalysis() {
        isAnyDepartmentOverrun = false;
        double accumulatedGrossDocumentCost = 0.0;

        costAllocationMap.clear();
        Map<Long, Department> departmentRefMap = new HashMap<>();

        for (PurchaseRequestLine prLine : workingPrLinesList) {
            if (prLine.getPurchaseRequestHeader() != null && prLine.getPurchaseRequestHeader().getForDepartment() != null) {
                Department dept = prLine.getPurchaseRequestHeader().getForDepartment();
                Long deptId = dept.getDepartmentId();

                PurchaseOrderLine polInstance = null;
                for (PurchaseOrderLine l : workingPoLinesList) {
                    if (prLine.getPurchaseOrderLine() != null && Objects.equals(l.getId(), prLine.getPurchaseOrderLine().getId())) {
                        polInstance = l;
                        break;
                    }
                }

                double poContractUnitPrice = polInstance != null && polInstance.getUnitPrice() != null ? polInstance.getUnitPrice() : 0.0;
                double activeOrderedQty = prLine.getOrderedQuantity() != null ? prLine.getOrderedQuantity() : 0.0;
                double basePrLineGrossValue = poContractUnitPrice * activeOrderedQty;
                
                if (polInstance != null && polInstance.getQuantity() != null && polInstance.getQuantity() > 0) {
                    double poLineQty = polInstance.getQuantity();
                    double poLineDiscountAmt = polInstance.getDiscountAmount() != null ? polInstance.getDiscountAmount() : 0.0;

                    double totalPoLineGrossValue = poContractUnitPrice * poLineQty;
                    double discountWeightRatio = poLineDiscountAmt / totalPoLineGrossValue;
                    
                    basePrLineGrossValue = basePrLineGrossValue - (basePrLineGrossValue * (Double.isNaN(discountWeightRatio) ? 0.0 : discountWeightRatio));
                }

                costAllocationMap.put(deptId, costAllocationMap.getOrDefault(deptId, 0.0) + basePrLineGrossValue);
                departmentRefMap.put(deptId, dept);
            }
        }

        for (PurchaseOrderLine poLine : workingPoLinesList) {
            if (poLine.getTotalAmount() != null) {
                accumulatedGrossDocumentCost += poLine.getTotalAmount();
            }
        }

        poTotalAmountText.setText("Consolidated Purchase Order Net Total: " + String.format("%.2f INR", accumulatedGrossDocumentCost));
        poTotalAmountText.getStyle().set("font-weight", "bold").set("font-size", "16px");

        workingBudgetList.clear();
        for (Map.Entry<Long, Double> datasetEntry : costAllocationMap.entrySet()) {
            Long deptId = datasetEntry.getKey();
            Department deptObj = departmentRefMap.get(deptId);
            Double totalNeededAmt = datasetEntry.getValue();

            DepartmentBudget accountLedger = departmentBudgetService.getByDepartmentAndYear(deptObj, Year.now());
            double availableCapital = (accountLedger != null && accountLedger.getRemainingBudgetAmount() != null) 
                    ? accountLedger.getRemainingBudgetAmount() : 0.0;
            double evaluatedBalanceLeft = availableCapital - totalNeededAmt;

            if (evaluatedBalanceLeft < 0 || accountLedger == null) {
                isAnyDepartmentOverrun = true;
            }

            workingBudgetList.add(deptObj);
        }

        budgetGrid.setItems(workingBudgetList);
        budgetGrid.getDataProvider().refreshAll();

        if (isAnyDepartmentOverrun) {
            approveBtn.setEnabled(false);
            approveBtn.setText("Locked (Budget Overrun Exception)");
        } else {
            approveBtn.setEnabled(canUserActionApproval);
            approveBtn.setText("Authorize Level");
        }
    }

    private void executeLevelApprovalTransaction() {
        if (isAnyDepartmentOverrun) {
            Notification.show("Compliance Block: Action rejected due to department general ledger overruns.", 4000, Position.TOP_CENTER);
            return;
        }

        try {
            Employee actionActor = securityService.getLoggedInUser().getEmployee();

            for (PurchaseRequestLine prline : workingPrLinesList) {
                prLineService.updatePurchaseRequestLine(prline, actionActor);
            }
            for (PurchaseOrderLine poline : workingPoLinesList) {
                poLineService.savePurchaseOrderLine(poline);
            }

            double finalCalculatedTotal = 0.0;
            for (PurchaseOrderLine l : workingPoLinesList) {
                finalCalculatedTotal += l.getTotalAmount() != null ? l.getTotalAmount() : 0.0;
            }
            poHeader.setTotalAmount(finalCalculatedTotal);
            poHeaderService.savePurchaseOrderHeader(poHeader);

            approvalTask.setStatus(Status.APPROVED);
            approvalTask.setComments(commentsField.getValue().trim());
            approvalTask.setApprovedDate(LocalDate.now());
            approvalTask.setApprover(actionActor);
            approvalsService.updateApprovals(approvalTask, actionActor);

            Notification.show("Level sign-off registered. Advanced to next workflow authority tier.", 4000, Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate("purchase-order"));

        } catch (Exception ex) {
            Notification.show("Transaction execution failed: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void executeWorkflowRejectionTransaction() {
        if (commentsField.isEmpty()) {
            Notification.show("Rejection requires descriptive comments context logs.", 3000, Position.TOP_CENTER);
            return;
        }
        try {
            Employee actionActor = securityService.getLoggedInUser().getEmployee();

            for (PurchaseRequestLine prline : workingPrLinesList) {
                prline.setOrderedQuantity(0.0);
                prLineService.updatePurchaseRequestLine(prline, actionActor);
            }
            
            for (PurchaseOrderLine poline : workingPoLinesList) {
                poline.setQuantity(0.0);
                poline.setDiscountAmount(0.0);
                poline.setTotalAmount(0.0);
                poLineService.savePurchaseOrderLine(poline);
            }
            
            poHeader.setTotalAmount(0.0);
            poHeader.setStatus(Status.REJECTED);
            poHeaderService.savePurchaseOrderHeader(poHeader);

            approvalTask.setStatus(Status.REJECTED);
            approvalTask.setComments(commentsField.getValue().trim());
            approvalTask.setApprovedDate(LocalDate.now());
            approvalTask.setApprover(actionActor);
            approvalsService.updateApprovals(approvalTask, actionActor);

            Notification.show("Purchase Order document rejected back to modification pools.", 3000, Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate("purchase-order"));

        } catch (Exception ex) {
            Notification.show("Rejection failed: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }
}