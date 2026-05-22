package com.module.purchase.view.purchaseOrder;

import java.util.List;

import com.module.purchase.entity.Department;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-draft", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderDraftView extends VerticalLayout {

    private final PurchaseOrderHeaderService poService;

    private final DepartmentService departmentService;

    private final Grid<PurchaseOrderDTO> grid =
            new Grid<>(PurchaseOrderDTO.class, false);


    private final TextField poIdField =
            new TextField("PO ID");

    private final TextField prIdField =
            new TextField("PR ID");

    private final ComboBox<Department> departmentField =
            new ComboBox<>("Department");

    private final ComboBox<Status> statusField =
            new ComboBox<>("Status");

    private PurchaseOrderDTO filter =
            new PurchaseOrderDTO();

    public PurchaseOrderDraftView(

            PurchaseOrderHeaderService poService,

            DepartmentService departmentService) {

        this.poService = poService;

        this.departmentService = departmentService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        buildUI();

        loadDrafts();
    }

    // =========================================================
    // UI
    // =========================================================

    private void buildUI() {

        H2 title =
                new H2("Draft Purchase Orders");

        configureFilters();

        configureGrid();

        add(

                title,

                buildFilterLayout(),

                grid
        );

        expand(grid);
    }

    private HorizontalLayout buildFilterLayout() {

        Button searchBtn =
                new Button("Search");

        Button clearBtn =
                new Button("Clear");

        searchBtn.addClickListener(event ->
                applyFilter());

        clearBtn.addClickListener(event ->
                clearFilter());

        HorizontalLayout layout =
                new HorizontalLayout(

                        poIdField,

                        prIdField,

                        departmentField,

                        statusField,

                        searchBtn,

                        clearBtn
                );

        layout.setAlignItems(Alignment.END);

        return layout;
    }

    private void configureFilters() {

        departmentField.setItems(
                departmentService.getDepartments()
        );

        departmentField.setItemLabelGenerator(
                Department::getDepartmentName
        );

        statusField.setItems(
                Status.DRAFT
        );

        statusField.setValue(
                Status.DRAFT
        );
    }

    private void configureGrid() {

        grid.removeAllColumns();


        grid.addComponentColumn(po -> {

            Button btn =
                    new Button(
                            String.valueOf(
                                    po.getPurchaseOrderId()
                            )
                    );

            btn.addClickListener(event ->

                    getUI().ifPresent(ui ->

                            ui.navigate(
                                    "purchase-order-form/"
                                            + po.getPurchaseOrderId()
                            )
                    )
            );

            return btn;

        }).setHeader("PO ID");

        // ================= PR ID =================

        grid.addColumn(po ->

                po.getPurchaseRequestHeader() != null

                        ? po.getPurchaseRequestHeader()
                        .getPurchaseRequestId()

                        : null

        ).setHeader("PR ID");

        // ================= DEPARTMENT =================

        grid.addColumn(po ->

                po.getPurchaseRequestHeader().getForDepartment() != null

                        ? po.getPurchaseRequestHeader().getForDepartment() .getDepartmentName()

                        : ""

        ).setHeader("Department");

        // ================= TOTAL =================

        grid.addColumn(
                PurchaseOrderDTO::getTotalAmount
        ).setHeader("Total Amount");

        // ================= STATUS =================

        grid.addColumn(
                PurchaseOrderDTO::getStatus
        ).setHeader("Status");

        grid.setWidthFull();

        grid.setHeightFull();
    }

    // =========================================================
    // APPLY FILTER
    // =========================================================

    private void applyFilter() {

        filter =
                new PurchaseOrderDTO();

        if (!poIdField.isEmpty()) {

            filter.setPurchaseOrderId(

                    Long.valueOf(
                            poIdField.getValue()
                    )
            );
        }

        if (!prIdField.isEmpty()) {

            // OPTIONAL
            // ONLY if service supports PR filter
        }

        filter.setForDepartment(
                departmentField.getValue()
        );

        filter.setStatus(
                statusField.getValue()
        );

        loadDrafts();
    }

    // =========================================================
    // CLEAR FILTER
    // =========================================================

    private void clearFilter() {

        poIdField.clear();

        prIdField.clear();

        departmentField.clear();

        statusField.setValue(
                Status.DRAFT
        );

        filter =
                new PurchaseOrderDTO();

        filter.setStatus(
                Status.DRAFT
        );

        loadDrafts();
    }

    // =========================================================
    // LOAD
    // =========================================================

    private void loadDrafts() {

        if (filter.getStatus() == null) {

            filter.setStatus(
                    Status.DRAFT
            );
        }

        List<PurchaseOrderDTO> drafts =

                poService
                        .getAllPurchaseOrdersfilter(
                                filter
                        );

        grid.setItems(drafts);
    }
}