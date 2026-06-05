package com.module.purchase.view.purchaseOrder;

import java.util.List;

import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-details/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseOrderHeaderService headerService;
    private final PurchaseOrderLineService lineService;

    private PurchaseOrderHeader header;

    private final Span orderId = new Span();
    private final Span requestId = new Span();
    private final Span createdBy = new Span();
    private final Span department = new Span();
    private final Span vendor = new Span();
    private final Span totalAmount = new Span();
    private final Span createdDate = new Span();
    private final Span status = new Span();

    private final Grid<PurchaseOrderLine> lineGrid = new Grid<>(PurchaseOrderLine.class, false);

    public PurchaseOrderDetailsView( PurchaseOrderHeaderService headerService, PurchaseOrderLineService lineService ) {

        this.headerService = headerService;
        this.lineService = lineService;
        
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        configureGrids();

        VerticalLayout headerSection = buildHeaderSection();
        headerSection.setWidthFull();

        VerticalLayout content = new VerticalLayout(
                new H2("Purchase Order Details"),
                headerSection,
                new H3("Line Items"),
                lineGrid 
        );

        content.setWidthFull();
        content.setPadding(true);
        content.setSpacing(true);

        Scroller scroller = new Scroller(content);
        scroller.setSizeFull();

        add(scroller);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Long id = Long.valueOf(
                event.getRouteParameters().get("id").get()
        );

        header = headerService.getPurchaseOrderHeaderById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));

        bindHeader();
        loadGrids();
    }

    private VerticalLayout buildHeaderSection() {

        VerticalLayout layout = new VerticalLayout(
                orderId,
                requestId,
                createdBy,
                department,
                vendor,
                totalAmount,
                createdDate,
                status
        );

        layout.setPadding(true);
        layout.setSpacing(false);

        layout.getStyle()
                .set("background", "#f9f9f9")
                .set("border", "1px solid #ddd")
                .set("border-radius", "8px");

        return layout;
    }

    private void bindHeader() {

        orderId.setText("PO ID : " + header.getPurchaseOrderId());

        createdBy.setText("Created By : " +
                (header.getCreatedBy() != null
                        ? header.getCreatedBy().getEmployeeName()
                        : "Auto"));

        vendor.setText("Vendor : " +
                (header.getVendor() != null ? header.getVendor().getVendorName() : "-"));

        totalAmount.setText("Total : " + header.getTotalAmount());
        createdDate.setText("Created : " + header.getCreatedDate());
        status.setText("Status : " + header.getStatus());
    }

    private void configureGrids() {


        lineGrid.addColumn(PurchaseOrderLine::getQuantity)
                .setHeader("Quantity");

        lineGrid.addColumn(PurchaseOrderLine::getUnitPrice)
                .setHeader("Unit Price");
        

        lineGrid.setWidthFull();
        lineGrid.setAllRowsVisible(true);
    }

    private void loadGrids() {

        List<PurchaseOrderLine> lines =
                lineService.getPurchaseOrderLineByHeader(header);

        lineGrid.setItems(lines);
    }
}