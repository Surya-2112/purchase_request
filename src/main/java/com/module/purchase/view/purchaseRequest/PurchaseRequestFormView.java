package com.module.purchase.view.purchaseRequest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-form", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestFormView extends VerticalLayout {

    private final PurchaseRequestHeaderService headerService;
    private final DepartmentService departmentService;
    private final ItemService itemService;
    private final PurchaseRequestLineService lineService;

    private final SecurityService securityService;

    private final DatePicker createdDateField = new DatePicker("Created Date");
    private final ComboBox<Department> departmentField = new ComboBox<>("Department");

    private final ComboBox<Item> itemField = new ComboBox<>("Item");
    private final IntegerField quantityField = new IntegerField("Quantity");
    private final NumberField unitPriceField = new NumberField("Unit Price");
    private final NumberField discountField = new NumberField("Discount");

    private final List<PurchaseRequestLine> lines = new ArrayList<>();
    private final Grid<PurchaseRequestLine> lineGrid = new Grid<>(PurchaseRequestLine.class, false);

    private final Button saveButton = new Button("Save & Go To Approval");

    public PurchaseRequestFormView(
            PurchaseRequestHeaderService headerService,
            DepartmentService departmentService,
            ItemService itemService,
            SecurityService securityService, PurchaseRequestLineService lineService) {

        this.headerService = headerService;
        this.departmentService = departmentService;
        this.itemService = itemService;
        this.securityService = securityService;
        this.lineService=lineService;

        setSizeFull();

        departmentField.setItems(departmentService.getDepartments());
        departmentField.setItemLabelGenerator(Department::getDepartmentName);

        itemField.setItems(itemService.getItems());
        itemField.setItemLabelGenerator(Item::getItemName);

        createdDateField.setValue(LocalDate.now());

        configureGrid();

        Button addLine = new Button("Add Line", e -> addLine());

        HorizontalLayout lineInput = new HorizontalLayout(
                itemField, quantityField, unitPriceField, discountField, addLine);

        saveButton.addClickListener(e -> saveAndGoApproval());

        add(
                new H2("Purchase Request Form"),
                createdDateField,
                departmentField,
                new H3("Lines"),
                lineInput,
                lineGrid,
                saveButton
        );
    }

    private void configureGrid() {

        lineGrid.addColumn(l -> l.getItem() == null ? "" : l.getItem().getItemName())
                .setHeader("Item");

        lineGrid.addColumn(PurchaseRequestLine::getQuantity).setHeader("Qty");
        lineGrid.addColumn(PurchaseRequestLine::getUnitPrice).setHeader("Price");
        lineGrid.addColumn(PurchaseRequestLine::getTotalPrice).setHeader("Total");

        lineGrid.setItems(lines);
    }

    private void addLine() {

        if (itemField.isEmpty()) {
            Notification.show("Select item");
            return;
        }

        PurchaseRequestLine line = new PurchaseRequestLine();
        line.setItem(itemField.getValue());
        line.setQuantity(quantityField.getValue());
        line.setUnitPrice(unitPriceField.getValue());
        line.setDiscount(discountField.getValue());

        double qty = quantityField.getValue() == null ? 0 : quantityField.getValue();
        double price = unitPriceField.getValue() == null ? 0 : unitPriceField.getValue();
        double discount = discountField.getValue() == null ? 0 : discountField.getValue();

        line.setTotalPrice((qty * price) - discount);

        lines.add(line);
        lineGrid.getDataProvider().refreshAll();

        clearLine();
    }

    private void clearLine() {
        itemField.clear();
        quantityField.setValue(1);
        unitPriceField.setValue(0.0);
        discountField.setValue(0.0);
    }

   private void saveAndGoApproval() {

    if (departmentField.isEmpty() || lines.isEmpty()) {
        Notification.show("Department and lines required");
        return;
    }

    PurchaseRequestHeader header = new PurchaseRequestHeader();

    header.setCreatedDate(Date.valueOf(createdDateField.getValue()));
    header.setForDepartment(departmentField.getValue());
    header.setStatus(Status.DRAFT);


    Employee currentUser = securityService.getLoggedInUser().getEmployee();
    header.setCreatedBy(currentUser);

    double total = lines.stream()
            .mapToDouble(PurchaseRequestLine::getTotalPrice)
            .sum();

    header.setTotalAmount(total);

    header.setPurchaseRequestLines(lines);

    PurchaseRequestHeader saved = headerService.addPurchaseRequestHeader(header,securityService.getLoggedInUser().getEmployee());

     for (PurchaseRequestLine l : lines) {
        l.setPurchaseRequestHeader(saved);
        lineService.addPurchaseRequestLine(l);
    }


    Notification.show("Saved. Go to Approval screen.");

    getUI().ifPresent(ui ->
            ui.navigate("purchase-request-approval/" + saved.getPurchaseRequestId()));
}
}