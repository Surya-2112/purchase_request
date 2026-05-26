package com.module.purchase.view.purchaseOrder;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Users;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
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

    private final AssigningApprovalsService assigningApprovalsService;

    private final SecurityService securityService;

    private final VendorService vendorService;

    private final Grid<PurchaseOrderDTO> poGrid = new Grid<>(PurchaseOrderDTO.class, false);

    private final Grid<AssigningApprovalsDTO> assignGrid = new Grid<>(AssigningApprovalsDTO.class, false);

    private int currentPage = 0;

    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private String viewMode = "ALL";

    private PurchaseOrderDTO poFilter = new PurchaseOrderDTO();

    private AssigningApprovalsDTO assignFilter = new AssigningApprovalsDTO();

    private final TextField poIdField = new TextField("PO ID");

    private final ComboBox<Status> poStatusField =new ComboBox<>("Status");

    private final ComboBox<Vendor> vendorField = new ComboBox<>("Vendor");

    private final TextField approvalIdField = new TextField("Approval ID");

    private final TextField referenceIdField =new TextField("PO ID");

    private final ComboBox<Status> approvalStatusField = new ComboBox<>("Status");

    private HorizontalLayout poFilters;

    private HorizontalLayout assignFilters;

    public PurchaseOrderView( PurchaseOrderHeaderService poService,  AssigningApprovalsService assigningApprovalsService,
            SecurityService securityService,VendorService vendorService) {

        this.poService = poService;

        this.assigningApprovalsService = assigningApprovalsService;

        this.securityService = securityService;

        this.vendorService = vendorService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        assignFilter.setStatus( Status.WAITING_APPROVAL);

        buildUI();

        loadData();
    }

    private void buildUI() {

        H2 title =  new H2("Purchase Orders");

        Button makeOrderBtn = new Button("Make Order");

        makeOrderBtn.addClickListener(event ->
                getUI().ifPresent(ui ->ui.navigate(
                                "purchase-order-draft" )
                )
        );

        HorizontalLayout headerLayout = new HorizontalLayout(title
                //,makeOrderBtn
        );

        headerLayout.setWidthFull();

        headerLayout.setJustifyContentMode( JustifyContentMode.BETWEEN);

        Button allBtn = new Button("Purchase Orders");

        Button assignedBtn =new Button("Assigned To You");

        Button createdBtn =new Button("Created By You");

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

       // HorizontalLayout tabs = new HorizontalLayout(allBtn,assignedBtn, createdBtn);

        poStatusField.setItems( Status.values());

        vendorField.setItems(vendorService.getVendors() );

        vendorField.setItemLabelGenerator( Vendor::getVendorName );

        Button poSearch = new Button("Search", e -> applyPOFilter());

        Button poClear = new Button( "Clear", e -> clearPOFilter() );

        poFilters = new HorizontalLayout();
        poFilters.setAlignItems(Alignment.END);
        poFilters.add(poIdField, 
                // poStatusField, 
                vendorField,poSearch,poClear);

        approvalStatusField.setItems( Status.WAITING_APPROVAL, Status.APPROVED,Status.REJECTED);

        approvalStatusField.setValue( Status.WAITING_APPROVAL);

        Button assignSearch = new Button( "Search",
                        e -> applyAssignFilter());

        Button assignClear =new Button("Clear",
                        e -> clearAssignFilter());

        assignFilters = new HorizontalLayout(approvalIdField, referenceIdField, approvalStatusField,assignSearch,assignClear);
        assignFilters.setAlignItems(Alignment.END);
        
        configurePOGrid();

        configureAssignGrid();

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
              //   tabs, 
                 poFilters,
                  assignFilters,
                   poGrid, 
                 //  assignGrid, 
                   pagination);

        expand(poGrid);

        expand(assignGrid);
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

        poGrid.addColumn(
                PurchaseOrderDTO::getTotalAmount
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

    // =========================================================
    // APPROVAL GRID
    // =========================================================

    private void configureAssignGrid() {

        assignGrid.removeAllColumns();

        assignGrid.addComponentColumn(a -> {

            Button btn = new Button(String.valueOf(a.getAssigningApprovalsId()));

            btn.addClickListener(event ->

                    getUI().ifPresent(ui ->

                            ui.navigate(
                                    "assigning-approvals-orders-details/"
                                            + a.getAssigningApprovalsId()
                            )
                    )
            );

            return btn;

        }).setHeader("Approval ID");

        assignGrid.addColumn( AssigningApprovalsDTO::getReferenceId).setHeader("PO ID");

        assignGrid.addColumn(a ->

                a.getApprover() != null

                        ? a.getApprover()
                        .getEmployeeName()

                        : ""

        ).setHeader("Approver");

        assignGrid.addColumn(AssigningApprovalsDTO::getLevel).setHeader("Level");

        assignGrid.addColumn( AssigningApprovalsDTO::getStatus).setHeader("Status");

        assignGrid.setWidthFull();

        assignGrid.setHeightFull();
    }

    private void loadData() {

        Users user = securityService.getLoggedInUser();

        poGrid.setVisible(false);

        assignGrid.setVisible(false);

        poFilters.setVisible(false);

        assignFilters.setVisible(false);

        if ("ASSIGNED".equals(viewMode)) {

            assignGrid.setVisible(true);

            assignFilters.setVisible(true);

            Page<AssigningApprovalsDTO> page =

                    assigningApprovalsService.getPurchaseOrderApprovalsForMe(

                                    assignFilter,

                                    user.getUserId(),

                                    currentPage,

                                    pageSize
                            );

            assignGrid.setItems(
                    page.getContent()
            );

            pageInfo.setText(

                    "Page "
                            + (currentPage + 1)
                            + " of "
                            + page.getTotalPages()
            );
        }

        else if ("CREATED".equals(viewMode)) {

            poGrid.setVisible(true);

            poFilters.setVisible(true);

            Page<PurchaseOrderDTO> page = poService.getCreatedByUser( poFilter, user.getUserId(), currentPage, pageSize);

            poGrid.setItems( page.getContent()
                            .stream()
                            .filter(po -> po.getStatus() != Status.DRAFT).toList()
            );

            pageInfo.setText(
                    "Page "
                            + (currentPage + 1)
                            + " of "
                            + page.getTotalPages()
            );
        }
        else {

            poGrid.setVisible(true);

            poFilters.setVisible(true);

            Page<PurchaseOrderDTO> page =

                    poService.getAllPurchaseOrder( poFilter, currentPage,pageSize );

            poGrid.setItems(

                    page.getContent()
                            .stream()
                            .filter(po -> po.getStatus()!= Status.DRAFT)
                            .toList()
            );

            pageInfo.setText(

                    "Page "
                            + (currentPage + 1)
                            + " of "
                            + page.getTotalPages()
            );
        }
    }

    private void applyPOFilter() {

        poFilter = new PurchaseOrderDTO();

        if (!poIdField.isEmpty()) {

            poFilter.setPurchaseOrderId(

                    Long.valueOf( poIdField.getValue())
            );
        }

        poFilter.setStatus(poStatusField.getValue() );

        poFilter.setVendor( vendorField.getValue());

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

    private void applyAssignFilter() {

        assignFilter = new AssigningApprovalsDTO();

        if (!approvalIdField.isEmpty()) { assignFilter.setAssigningApprovalsId(
                    Long.valueOf(  approvalIdField.getValue() )
            );
        }

        if (!referenceIdField.isEmpty()) {

            assignFilter.setReferenceId(Long.valueOf(referenceIdField.getValue()));
        }

        assignFilter.setStatus(approvalStatusField.getValue());

        currentPage = 0;

        loadData();
    }

    private void clearAssignFilter() {

        approvalIdField.clear();

        referenceIdField.clear();

        approvalStatusField.setValue(
                Status.WAITING_APPROVAL
        );

        assignFilter =
                new AssigningApprovalsDTO();

        assignFilter.setStatus(
                Status.WAITING_APPROVAL
        );

        currentPage = 0;

        loadData();
    }
}