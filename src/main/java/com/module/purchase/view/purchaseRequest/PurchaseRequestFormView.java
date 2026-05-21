package com.module.purchase.view.purchaseRequest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.enums.Status;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-form/:id?", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestFormView extends VerticalLayout
        implements BeforeEnterObserver {

    private final PurchaseRequestHeaderService headerService;

    private final DepartmentService departmentService;

    private final ItemService itemService;

    private final PurchaseRequestLineService lineService;

    private final SecurityService securityService;

    // ================= FORM =================

    private final DatePicker createdDateField =
            new DatePicker("Created Date");

    private final ComboBox<Department> departmentField =
            new ComboBox<>("Department");

    private final ComboBox<Item> itemField =
            new ComboBox<>("Item");

    private final IntegerField quantityField =
            new IntegerField("Quantity");

    private final NumberField unitPriceField =
            new NumberField("Unit Price");

    private final NumberField discountField =
            new NumberField("Discount");

    // ================= DATA =================

    private final List<PurchaseRequestLine> lines =
            new ArrayList<>();

    private PurchaseRequestHeader editingHeader = null;

    private PurchaseRequestLine editingLine = null;

    // ================= GRID =================

    private final Grid<PurchaseRequestLine> lineGrid =
            new Grid<>(PurchaseRequestLine.class, false);

    // ================= BUTTON =================

    private final Button saveButton =
            new Button("Save & Go To Approval");

    // ================= CONSTRUCTOR =================

    public PurchaseRequestFormView(

            PurchaseRequestHeaderService headerService,

            DepartmentService departmentService,

            ItemService itemService,

            SecurityService securityService,

            PurchaseRequestLineService lineService) {

        this.headerService = headerService;

        this.departmentService = departmentService;

        this.itemService = itemService;

        this.securityService = securityService;

        this.lineService = lineService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        // ================= LOAD DEPARTMENTS =================

        departmentField.setItems(
                departmentService.getDepartments()
        );

        departmentField.setItemLabelGenerator(
                Department::getDepartmentName
        );

        // ================= LOAD ITEMS =================

        itemField.setItems(
                itemService.getItems()
        );

        itemField.setItemLabelGenerator(
                Item::getItemName
        );

        // ================= DEFAULT VALUES =================

        createdDateField.setValue(
                LocalDate.now()
        );

        quantityField.setValue(1);

        unitPriceField.setValue(0.0);

        discountField.setValue(0.0);

        configureGrid();

        // ================= BUTTONS =================

        Button addLineButton =
                new Button(
                        "Add / Update Line",
                        e -> addLine()
                );

        saveButton.addClickListener(
                e -> saveAndGoApproval()
        );

        // ================= INPUT LAYOUT =================

        HorizontalLayout lineInput =
                new HorizontalLayout(

                        itemField,

                        quantityField,

                        unitPriceField,

                        discountField,

                        addLineButton
                );

        lineInput.setAlignItems(
                Alignment.END
        );

        // ================= UI =================

        add(

                new H2("Purchase Request Form"),

                createdDateField,

                departmentField,

                new H3("Purchase Request Lines"),

                lineInput,

                lineGrid,

                saveButton
        );
    }

    // ================= ROUTE =================

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Optional<String> parameter =

                event.getRouteParameters()
                        .get("id");

        if (parameter.isPresent()) {

            Long id =
                    Long.parseLong(
                            parameter.get()
                    );

            editingHeader =
                    headerService
                            .getPurchaseRequestHeaderById(id)
                            .orElse(null);

            if (editingHeader == null) {

                Notification.show(
                        "Purchase Request Not Found"
                );

                return;
            }

            loadEditData();
        }
    }

    // ================= LOAD EDIT =================

    private void loadEditData() {

        createdDateField.setValue(

                editingHeader
                        .getCreatedDate()
                        .toLocalDate()
        );

        departmentField.setValue(
                editingHeader.getForDepartment()
        );

        lines.clear();

        lines.addAll(

                lineService
                        .getPurchaseRequestLineByHeader(
                                editingHeader
                        )
        );

        lineGrid.getDataProvider()
                .refreshAll();

        saveButton.setText(
                "Update & Go To Approval"
        );
    }

    // ================= GRID =================

    private void configureGrid() {

        lineGrid.removeAllColumns();

        lineGrid.addColumn(line ->

                        line.getItem() != null

                                ? line.getItem()
                                .getItemName()

                                : ""
                )

                .setHeader("Item")

                .setAutoWidth(true);

        lineGrid.addColumn(
                        PurchaseRequestLine::getQuantity
                )
                .setHeader("Quantity");

        lineGrid.addColumn(
                        PurchaseRequestLine::getUnitPrice
                )
                .setHeader("Unit Price");

        lineGrid.addColumn(
                        PurchaseRequestLine::getDiscount
                )
                .setHeader("Discount");

        lineGrid.addColumn(
                        PurchaseRequestLine::getTotalPrice
                )
                .setHeader("Total");

        // ================= ACTIONS =================

        lineGrid.addComponentColumn(line -> {

            Button editButton =
                    new Button("Edit");

            editButton.addClickListener(
                    e -> editLine(line)
            );

            Button deleteButton =
                    new Button("Delete");

            deleteButton.addClickListener(e -> {

                lines.remove(line);

                lineGrid.getDataProvider()
                        .refreshAll();

                Notification.show(
                        "Line deleted"
                );
            });

            return new HorizontalLayout(
                    editButton,
                    deleteButton
            );

        }).setHeader("Actions");

        lineGrid.setItems(lines);

        lineGrid.setWidthFull();

        lineGrid.setAllRowsVisible(true);
    }

    // ================= ADD / UPDATE LINE =================

    private void addLine() {

        if (itemField.isEmpty()) {

            Notification.show(
                    "Please select item"
            );

            return;
        }

        Integer qtyValue =
                quantityField.getValue();

        Double priceValue =
                unitPriceField.getValue();

        Double discountValue =
                discountField.getValue();

        int qty =
                qtyValue == null ? 0 : qtyValue;

        double price =
                priceValue == null ? 0 : priceValue;

        double discount =
                discountValue == null
                        ? 0
                        : discountValue;

        double total =
                (qty * price) - discount;

        // ================= UPDATE =================

        if (editingLine != null) {

            editingLine.setItem(
                    itemField.getValue()
            );

            editingLine.setQuantity(qty);

            editingLine.setUnitPrice(price);

            editingLine.setDiscount(discount);

            editingLine.setTotalPrice(total);

            Notification.show(
                    "Line updated"
            );

            editingLine = null;

        } else {

            // ================= ADD =================

            PurchaseRequestLine line =
                    new PurchaseRequestLine();

            line.setItem(
                    itemField.getValue()
            );

            line.setQuantity(qty);

            line.setUnitPrice(price);

            line.setDiscount(discount);

            line.setTotalPrice(total);

            lines.add(line);

            Notification.show(
                    "Line added"
            );
        }

        lineGrid.getDataProvider()
                .refreshAll();

        clearLine();
    }

    // ================= EDIT LINE =================

    private void editLine(
            PurchaseRequestLine line) {

        editingLine = line;

        itemField.setValue(
                line.getItem()
        );

        quantityField.setValue(
                line.getQuantity()
        );

        unitPriceField.setValue(
                line.getUnitPrice()
        );

        discountField.setValue(
                line.getDiscount()
        );

        Notification.show(
                "Edit mode enabled"
        );
    }

    // ================= CLEAR =================

    private void clearLine() {

        itemField.clear();

        quantityField.setValue(1);

        unitPriceField.setValue(0.0);

        discountField.setValue(0.0);

        editingLine = null;
    }

    // ================= SAVE =================

    private void saveAndGoApproval() {

        if (departmentField.isEmpty()
                || lines.isEmpty()) {

            Notification.show(
                    "Department and lines required"
            );

            return;
        }

        Employee currentUser =

                securityService
                        .getLoggedInUser()
                        .getEmployee();

        double total = lines.stream()

                .mapToDouble(
                        PurchaseRequestLine::getTotalPrice
                )

                .sum();

        PurchaseRequestHeader saved;

        // ================= UPDATE =================

        if (editingHeader != null) {

            editingHeader.setCreatedDate(

                    Date.valueOf(
                            createdDateField.getValue()
                    )
            );

            editingHeader.setForDepartment(
                    departmentField.getValue()
            );

            editingHeader.setTotalAmount(total);

            editingHeader.setPurchaseRequestLines(
                    lines
            );

            saved =headerService.updatePurchaseRequestHeader(
                                    editingHeader,
                                    currentUser
                            );

            // DELETE OLD LINES
            lineService.deleteAllLine(saved);

            // SAVE NEW LINES
            for (PurchaseRequestLine line : lines) {

                line.setPurchaseRequestHeader(
                        saved
                );

                lineService.addPurchaseRequestLine(
                        line
                );
            }

            Notification.show(
                    "Purchase Request Updated"
            );

        } else {

            // ================= NEW SAVE =================

            PurchaseRequestHeader header =
                    new PurchaseRequestHeader();

            header.setCreatedDate(

                    Date.valueOf(
                            createdDateField.getValue()
                    )
            );

            header.setForDepartment(
                    departmentField.getValue()
            );

            header.setStatus(Status.DRAFT);

            header.setCreatedBy(currentUser);

            header.setTotalAmount(total);

            header.setPurchaseRequestLines(lines);

            saved =
                    headerService
                            .addPurchaseRequestHeader(
                                    header,
                                    currentUser
                            );

            for (PurchaseRequestLine line : lines) {

                line.setPurchaseRequestHeader(
                        saved
                );

                lineService.addPurchaseRequestLine(
                        line
                );
            }

            Notification.show(
                    "Purchase Request Saved"
            );
        }

        getUI().ifPresent(ui ->

                ui.navigate(

                        "purchase-request-approval/"
                                + saved
                                .getPurchaseRequestId()
                )
        );
    }
}