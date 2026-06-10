package com.module.purchase.view.purchaseOrder;

import org.springframework.data.domain.Page;

import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.VendorService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
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

    private final VendorService vendorService;

    private final Grid<PurchaseOrderDTO> poGrid = new Grid<>(PurchaseOrderDTO.class, false);

    private int currentPage = 0;

    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private PurchaseOrderDTO poFilter = new PurchaseOrderDTO();

    private final TextField poIdField = new TextField("PO ID");

    private final ComboBox<Status> poStatusField =new ComboBox<>("Status");
 
    private final ComboBox<Vendor> vendorField = new ComboBox<>("Vendor");

    private HorizontalLayout poFilters;

    public PurchaseOrderView( PurchaseOrderHeaderService poService,VendorService vendorService) {

        this.poService = poService;

        this.vendorService = vendorService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        buildUI();

        loadData();
    }

    private void buildUI() {

        H2 title =  new H2("Purchase Orders");

        HorizontalLayout headerLayout = new HorizontalLayout(title);

        headerLayout.setWidthFull();

        headerLayout.setJustifyContentMode( JustifyContentMode.BETWEEN);

        poStatusField.setItems( Status.values());

        vendorField.setItems(vendorService.getVendors() );

        vendorField.setItemLabelGenerator( Vendor::getVendorName );

        Button poSearch = new Button("Search", e -> applyPOFilter());

        Button poClear = new Button( "Clear", e -> clearPOFilter() );

        poFilters = new HorizontalLayout();
        poFilters.setAlignItems(Alignment.END);
        poFilters.add(poIdField,vendorField,poSearch,poClear);

        configurePOGrid();

        Button prev = new Button("Prev");

        Button next = new Button("Next");

        prev.addClickListener(event -> {
                if (currentPage > 0) {
                currentPage--;
                loadData(); }
        });

        next.addClickListener(event -> {
            currentPage++;
            loadData();});

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

        HorizontalLayout pagination = new HorizontalLayout(
                        prev,
                        pageInfo,
                        next,
                        new Span("Page Size"),
                        pageSizeField);
        
        pagination.setWidthFull();
        pagination.setJustifyContentMode( JustifyContentMode.CENTER);
        pagination.setAlignItems( Alignment.CENTER);

        add(headerLayout,
                 poFilters,
                   poGrid, 
                   pagination);

        expand(poGrid);
    }

    private void configurePOGrid() {

        poGrid.removeAllColumns();

        poGrid.addColumn( PurchaseOrderDTO::getPurchaseOrderId).setHeader("PO ID");

        poGrid.addColumn(po ->

                po.getVendor() != null

                        ? po.getVendor()
                        .getVendorName()

                        : ""

        ).setHeader("Vendor");

        // ================= TOTAL =================

        poGrid.addColumn(PurchaseOrderDTO::getTotalAmount
        ).setHeader("Total Amount");

        // ================= STATUS =================

        poGrid.addColumn(
                PurchaseOrderDTO::getStatus
        ).setHeader("Status");

        poGrid.setWidthFull();

        poGrid.setHeightFull();

        poGrid.addItemDoubleClickListener(event->{
               PurchaseOrderDTO po=event.getItem();

               getUI().ifPresent(ui ->
                        ui.navigate("purchase-order-details/"+ po.getPurchaseOrderId()));
        
        });
        poGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void loadData() {

            poGrid.setVisible(true);

            poFilters.setVisible(true);

            Page<PurchaseOrderDTO> page = poService.getAllPurchaseOrder( poFilter, currentPage,pageSize );

            poGrid.setItems(

                    page.getContent()
                            // .stream()
                            // .filter(po -> po.getStatus()!= Status.DRAFT)
                            // .toList()
            );

            pageInfo.setText(

                    "Page "
                            + (currentPage + 1)
                            + " of "
                            + page.getTotalPages()
            );
    }

    private void applyPOFilter() {

        poFilter = new PurchaseOrderDTO();

        if (!poIdField.isEmpty()) {

            poFilter.setPurchaseOrderId(

                    Long.valueOf( poIdField.getValue())
            );
        }

        poFilter.setStatus(poStatusField.getValue() );

        poFilter.setVendor(vendorField.getValue());

        currentPage = 0;

        loadData();
    }

    private void clearPOFilter() {

        poIdField.clear();

        poStatusField.clear();

        vendorField.clear();

        poFilter =  new PurchaseOrderDTO();

        currentPage = 0;

        loadData();
    }
}