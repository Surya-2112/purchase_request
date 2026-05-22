package com.module.purchase.view.purchaseOrder;

import java.time.LocalDate;
import java.time.Year;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.Status;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
import com.module.purchase.service.VendorService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-form/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderFormView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseOrderHeaderService poService;
    private final DepartmentBudgetService departmentBudgetService;
    private final VendorService vendorService;
    private final SecurityService securityService;
    private final PurchaseOrderLineService poLineService;

    private PurchaseOrderHeader poHeader;
    private DepartmentBudget budget;

    // UI
    private final Span poNumber = new Span();
    private final Span totalAmount = new Span();
    private final Span departmentName = new Span();

    private final Span budgetTotal = new Span();
    private final Span budgetRemaining = new Span();

    private final ComboBox<Vendor> vendorCombo = new ComboBox<>("Select Vendor");

    private final Grid<PurchaseOrderLine> lineGrid = new Grid<>(PurchaseOrderLine.class, false);

    private final Button saveBtn = new Button("Save & Send to Approval");
    private final Button cancelBtn = new Button("Cancel");

    public PurchaseOrderFormView(
            PurchaseOrderHeaderService poService,
            DepartmentBudgetService departmentBudgetService,
            VendorService vendorService,
            SecurityService securityService,
            PurchaseOrderLineService poLineService
    ) {
        this.poService = poService;
        this.departmentBudgetService = departmentBudgetService;
        this.vendorService = vendorService;
        this.securityService = securityService;
        this.poLineService = poLineService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureUI();
    }

    // ================= ROUTE LOAD =================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id = Long.parseLong(event.getRouteParameters().get("id").get());

        poHeader = poService.getPurchaseOrderHeaderById(id)
                .orElseThrow(() -> new RuntimeException("PO not found"));

        loadBudget();
        loadPO();
    }

    // ================= LOAD PO =================

    private void loadPO() {

        poNumber.setText("PO ID: " + poHeader.getPurchaseOrderId());
        totalAmount.setText("Total: " + poHeader.getTotalAmount());

        departmentName.setText(
                "Department: " + poHeader.getPurchaseRequestHeader()
                        .getForDepartment()
                        .getDepartmentName()
        );

        lineGrid.setItems(poLineService.getPurchaseOrderLineByHeader(poHeader));
    }

    private void loadBudget() {

        budget = departmentBudgetService.getByDepartmentAndYear(
                poHeader.getPurchaseRequestHeader().getForDepartment(),
                Year.now()
        );

        if (budget == null) {
            Notification.show(
                    "Department budget not configured",
                    3000,
                    Position.TOP_CENTER
            );

            saveBtn.setEnabled(false);
            cancelBtn.setEnabled(false);
            return;
        }

        budgetTotal.setText("Budget: " + budget.getTotalBudgetAmount());
        budgetRemaining.setText("Remaining Budget After Purchase: " + budget.getRemainingBudgetAmount());
    }

    // ================= UI =================

    private void configureUI() {

        vendorCombo.setItems(vendorService.getVendors());
        vendorCombo.setItemLabelGenerator(Vendor::getVendorName);

        saveBtn.addClickListener(e -> saveAndApprove());
        cancelBtn.addClickListener(e -> cancelPO());

        lineGrid.addColumn(po -> po.getItem().getItemName())
                .setHeader("Item");

        lineGrid.addColumn(PurchaseOrderLine::getQuantity)
                .setHeader("Qty");

        lineGrid.addColumn(PurchaseOrderLine::getUnitPrice)
                .setHeader("Unit Price");

        lineGrid.addColumn(PurchaseOrderLine::getTotalPrice)
                .setHeader("Total");

        VerticalLayout budgetBox = new VerticalLayout(
                budgetTotal,
                budgetRemaining
        );

        budgetBox.getStyle()
                .set("border", "1px solid #ccc")
                .set("padding", "10px");

        HorizontalLayout buttons = new HorizontalLayout(saveBtn, cancelBtn);

        add(
                new H2("Purchase Order"),
                poNumber,
                totalAmount,
                departmentName,
                budgetBox,
                vendorCombo,
                lineGrid,
                buttons
        );
    }

    private void saveAndApprove() {

        if (vendorCombo.getValue() == null) {
            Notification.show(
                    "Select vendor first",
                    3000,
                    Position.TOP_CENTER
            );
            return;
        }

        poHeader.setVendor(vendorCombo.getValue());
        poHeader.setStatus(Status.WAITING_APPROVAL);
        poHeader.setCreatedBy(securityService.getLoggedInUser().getEmployee());
        poHeader.setCreatedDate(LocalDate.now());

        poService.updatePurchaseOrderHeader(
                poHeader,
                securityService.getLoggedInUser().getEmployee()
        );

        Notification.show(
                "PO sent for approval",
                3000,
                Position.TOP_CENTER
        );

        getUI().ifPresent(ui ->
                ui.navigate("purchase-order-approval/" + poHeader.getPurchaseOrderId())
        );
    }

    // ================= CANCEL =================

    private void cancelPO() {

        poHeader.setStatus(Status.CANCELLED);
        poHeader.setCreatedBy(securityService.getLoggedInUser().getEmployee());
        poHeader.setCreatedDate(LocalDate.now());
        poService.updatePurchaseOrderHeader(
                poHeader,
                securityService.getLoggedInUser().getEmployee()
        );

        Notification.show(
                "Purchase Order Cancelled",
                3000,
                Position.TOP_CENTER
        );

        getUI().ifPresent(ui ->
                ui.navigate("purchase-order")
        );
    }
}