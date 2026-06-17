package com.module.purchase.view.purchaseOrder;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-details", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final PurchaseOrderHeaderService poHeaderService;
    private final PurchaseOrderLineService poLineService;
    private final AssigningApprovalsService approvalsService;

    private PurchaseOrderHeader poHeader;

    private final Span poIdText = new Span();
    private final Span vendorText = new Span();
    private final Span dateText = new Span();
    private final Span totalCostText = new Span();
    private final Span statusBadge = new Span();

    private final Grid<PurchaseOrderLine> itemsGrid = new Grid<>(PurchaseOrderLine.class, false);
    private final Grid<AssigningApprovals> historyGrid = new Grid<>(AssigningApprovals.class, false);

    private final Button backBtn = new Button("Back to PO Directory", VaadinIcon.ARROW_LEFT.create());

    public PurchaseOrderDetailsView(
            PurchaseOrderHeaderService poHeaderService,
            PurchaseOrderLineService poLineService,
            AssigningApprovalsService approvalsService) {

        this.poHeaderService = poHeaderService;
        this.poLineService = poLineService;
        this.approvalsService = approvalsService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        backBtn.addThemeName("tertiary small");
        backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("purchase-order")));

        configureItemsGrid();
        configureHistoryGrid();

        VerticalLayout metaFieldsCard = new VerticalLayout();
        metaFieldsCard.setWidthFull();
        metaFieldsCard.setSpacing(false);
        metaFieldsCard.setPadding(true);
        metaFieldsCard.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px")
                .set("background-color", "var(--lumo-contrast-5pct)");

        HorizontalLayout row1 = new HorizontalLayout(poIdText, statusBadge);
        row1.setSpacing(true);
        row1.setAlignItems(Alignment.CENTER);

        metaFieldsCard.add(row1, vendorText, dateText, totalCostText);

        HorizontalLayout toolbar = new HorizontalLayout(backBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        VerticalLayout layoutScrollerContent = new VerticalLayout(
                new H2("Purchase Order Document "),
                toolbar,
                metaFieldsCard,
                new Hr(),
                new H3("Procured Line Items "),
                itemsGrid,
                new Hr(),
                new H3("Approval List"),
                historyGrid
        );
        layoutScrollerContent.setWidthFull();
        layoutScrollerContent.setPadding(false);

        Scroller viewScroller = new Scroller(layoutScrollerContent);
        viewScroller.setSizeFull();
        add(viewScroller);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id == null) {
            Notification.show("Invalid Request Context Key Passed.", 3000, Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("purchase-order"));
            return;
        }

        poHeaderService.getPurchaseOrderHeaderById(id).ifPresentOrElse(po -> {
            this.poHeader = po;
            bindProfileData();
            loadAssociatedLineDatasets();
        }, () -> {
            Notification.show("Target Purchase Order Document reference file missing.", 4000, Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("purchase-order"));
        });
    }

    private void bindProfileData() {
        poIdText.setText("Purchase Order ID: PO-" + poHeader.getPurchaseOrderId());
        poIdText.getStyle().set("font-weight", "bold").set("font-size", "18px");

        vendorText.setText(" Vendor: " + (poHeader.getVendor() != null ? poHeader.getVendor().getVendorName() : "Unknown"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        String formattedDate = poHeader.getCreatedDate() != null ? poHeader.getCreatedDate().format(formatter) : "-";
        dateText.setText("Order  Date: " + formattedDate);
        
        totalCostText.setText(" Ordered Value: " + String.format("%.2f", poHeader.getTotalAmount()));
        totalCostText.getStyle().set("font-weight", "bold").set("color", "var(--lumo-primary-text-color)");

        statusBadge.setText(poHeader.getStatus() != null ? poHeader.getStatus().name() : "UNKNOWN");
        statusBadge.getStyle()
                .set("padding", "3px 10px")
                .set("border-radius", "4px")
                .set("font-weight", "bold")
                .set("font-size", "12px");

        if (poHeader.getStatus() == Status.APPROVED) {
            statusBadge.getStyle().set("background-color", "#e0f2fe").set("color", "#0369a1");
        } else if (poHeader.getStatus() == Status.WAITING_APPROVAL) {
            statusBadge.getStyle().set("background-color", "#fef3c7").set("color", "#b45309");
        } else if (poHeader.getStatus() == Status.REJECTED) {
            statusBadge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
        } else {
            statusBadge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
        }
    }

    private void configureItemsGrid() {
        itemsGrid.removeAllColumns();
        itemsGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item / Variant Description").setAutoWidth(true);
        itemsGrid.addColumn(PurchaseOrderLine::getQuantity).setHeader("Committed Qty").setWidth("120px");
        itemsGrid.addColumn(line -> String.format("%.2f INR", line.getUnitPrice())).setHeader("Unit Price").setWidth("130px");
        itemsGrid.addColumn(line -> String.format("%.2f INR", line.getDiscountAmount())).setHeader("Applied Discount").setWidth("140px");
        itemsGrid.addColumn(line -> String.format("%.2f INR", line.getTotalAmount())).setHeader("Net Total").setWidth("150px");

        itemsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        itemsGrid.setAllRowsVisible(true);
    }

    private void configureHistoryGrid() {
        historyGrid.removeAllColumns();
        historyGrid.addColumn(AssigningApprovals::getLevel).setHeader("Tier Level").setWidth("90px");
        historyGrid.addColumn(a -> a.getEmployeeGroup() != null ? a.getEmployeeGroup().getDisplayName() : "").setHeader("Authorization Group").setAutoWidth(true);
        historyGrid.addColumn(a -> a.getApprover() != null ? a.getApprover().getEmployeeName() : "Pending Step").setHeader("Actioned By Person").setAutoWidth(true);
        
        historyGrid.addColumn(a -> {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            return a.getApprovedDate() != null ? a.getApprovedDate().format(f) : "-";
        }).setHeader("Actioned Date").setWidth("140px");
        
        historyGrid.addColumn(AssigningApprovals::getStatus).setHeader("Step Decision").setWidth("130px");
        historyGrid.addColumn(AssigningApprovals::getComments).setHeader("Manager Review Annotations / Remarks").setAutoWidth(true);

        historyGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        historyGrid.setAllRowsVisible(true);
    }

    private void loadAssociatedLineDatasets() {
         PurchaseOrderLine poline=new PurchaseOrderLine();
        poline.setPurchaseOrderHeader(poHeader);
        List<PurchaseOrderLine> itemLines = poLineService.getPurchaseOrderList(poline);
        itemsGrid.setItems(itemLines);

        List<AssigningApprovals> approvalLogs = approvalsService.getAssigningApprovalByTypeAndReferId(
                ApprovalType.PURCHASE_ORDER, 
                poHeader.getPurchaseOrderId()
        );
        historyGrid.setItems(approvalLogs);
    }
}