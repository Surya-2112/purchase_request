package com.module.purchase.view.purchaseRequest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.Users;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.SecurityService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-form", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestFormView extends VerticalLayout {

    private final PurchaseRequestHeaderService purchaseRequestHeaderService;

    private final DepartmentService departmentService;

    private final EmployeeService employeeService;

    private final ItemService itemService;

    private final SecurityService securityService;

    // ================= HEADER =================

    private final DatePicker createdDateField =
            new DatePicker("Created Date");

    private final ComboBox<Department> departmentField =
            new ComboBox<>("Department");

    // ================= LINE INPUT =================

    private final ComboBox<Item> itemField =
            new ComboBox<>("Item");

    private final IntegerField quantityField =
            new IntegerField("Quantity");

    private final NumberField unitPriceField =
            new NumberField("Unit Price");

    private final NumberField discountField =
            new NumberField("Discount");

    // ================= APPROVAL INPUT =================

    private final ComboBox<Employee> approverField =
            new ComboBox<>("Approver");

    private final IntegerField levelField =
            new IntegerField("Level");

    // ================= DATA =================

    private final List<PurchaseRequestLine> lines =
            new ArrayList<>();

    private final List<AssigningApprovals> approvals =
            new ArrayList<>();

    // ================= GRID =================

    private final Grid<PurchaseRequestLine> lineGrid =
            new Grid<>(PurchaseRequestLine.class, false);

    private final Grid<AssigningApprovals> approvalGrid =
            new Grid<>(AssigningApprovals.class, false);

    // ================= TOTAL =================

    private final Span totalLabel =
            new Span("0.0");

    public PurchaseRequestFormView(

            PurchaseRequestHeaderService purchaseRequestHeaderService,

            DepartmentService departmentService,

            EmployeeService employeeService,

            ItemService itemService,

            SecurityService securityService,

            AssigningConfigService assigningConfigService) {

        this.purchaseRequestHeaderService =
                purchaseRequestHeaderService;

        this.departmentService =
                departmentService;

        this.employeeService =
                employeeService;

        this.itemService =
                itemService;

        this.securityService =
                securityService;

        // ================= PAGE =================

        setSizeFull();

        setPadding(false);

        setSpacing(false);

        // ================= MAIN CONTENT =================

        VerticalLayout content =
                new VerticalLayout();

        content.setWidthFull();

        content.setPadding(true);

        content.setSpacing(true);

        // ================= TITLE =================

        H2 title =
                new H2("Purchase Request Form");

        // ================= LOAD DATA =================

        departmentField.setItems(
                departmentService.getDepartments());

        departmentField.setItemLabelGenerator(
                Department::getDepartmentName);

        itemField.setItems(
                itemService.getItems());

        itemField.setItemLabelGenerator(
                Item::getItemName);

        approverField.setItems(
                employeeService.getEmployees());

        approverField.setItemLabelGenerator(
                Employee::getEmployeeName);

        createdDateField.setValue(
                LocalDate.now());

        quantityField.setValue(1);

        unitPriceField.setValue(0.0);

        discountField.setValue(0.0);

        levelField.setValue(1);

        // ================= HEADER FORM =================

        FormLayout headerForm =
                new FormLayout();

        headerForm.setWidthFull();

        headerForm.add(
                createdDateField,
                departmentField);

        headerForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2));

        // ================= LINE INPUT =================

        Button addLineButton =
                new Button("Add Line");

        addLineButton.addClickListener(
                e -> addLine());

        itemField.setWidth("300px");

        quantityField.setWidth("150px");

        unitPriceField.setWidth("150px");

        discountField.setWidth("150px");

        HorizontalLayout lineInput =
                new HorizontalLayout(
                        itemField,
                        quantityField,
                        unitPriceField,
                        discountField,
                        addLineButton);

        lineInput.setWidthFull();

        lineInput.setAlignItems(
                Alignment.END);

        // ================= CONFIGURE LINE GRID =================

        configureLineGrid();

        // ================= LINE SECTION =================

        VerticalLayout lineSection =
                new VerticalLayout();

        lineSection.setWidthFull();

        lineSection.setPadding(false);

        lineSection.setSpacing(true);

        lineSection.add(
                lineInput,
                lineGrid);

        // ================= LINE SCROLLER =================

        Scroller lineScroller =
                new Scroller(lineSection);

        lineScroller.setWidthFull();

        lineScroller.setHeight("400px");

        // ================= TOTAL =================

        HorizontalLayout totalLayout =
                new HorizontalLayout();

        totalLayout.setWidthFull();

        totalLayout.add(
                new H3("Total Amount : "),
                totalLabel);

        // ================= APPROVAL INPUT =================

        Button addApprovalButton =
                new Button("Add Approval");

        addApprovalButton.addClickListener(
                e -> addApproval());

        HorizontalLayout approvalInput =
                new HorizontalLayout(
                        approverField,
                        levelField,
                        addApprovalButton);

        approvalInput.setWidthFull();

        approvalInput.setAlignItems(
                Alignment.END);

        // ================= APPROVAL GRID =================

        configureApprovalGrid();

        // ================= SAVE =================

        Button saveButton =
                new Button("Save Purchase Request");

        saveButton.addClickListener(
                e -> savePurchaseRequest());

        // ================= ADD COMPONENTS =================

        content.add(

                title,

                new H3("Purchase Request Header"),

                headerForm,

                new H3("Purchase Request Lines"),

                lineScroller,

                totalLayout,

                new H3("Assigning Approvals"),

                approvalInput,

                approvalGrid,

                saveButton);

        add(content);
    }

    // ================= LINE GRID =================

    private void configureLineGrid() {

        lineGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS);

        lineGrid.addColumn(line ->
                line.getItem() == null
                        ? ""
                        : line.getItem().getItemName())
                .setHeader("Item");

        lineGrid.addColumn(
                PurchaseRequestLine::getQuantity)
                .setHeader("Quantity");

        lineGrid.addColumn(
                PurchaseRequestLine::getUnitPrice)
                .setHeader("Unit Price");

        lineGrid.addColumn(
                PurchaseRequestLine::getDiscount)
                .setHeader("Discount");

        lineGrid.addColumn(
                PurchaseRequestLine::getTotalPrice)
                .setHeader("Total");

        lineGrid.addComponentColumn(line -> {

            Button deleteButton =
                    new Button("Delete");

            deleteButton.addClickListener(event -> {

                lines.remove(line);

                lineGrid.getDataProvider()
                        .refreshAll();

                refreshTotal();
            });

            return deleteButton;

        }).setHeader("Action");

        lineGrid.setItems(lines);

        lineGrid.setWidthFull();

        lineGrid.setHeight("300px");
    }

    // ================= APPROVAL GRID =================

    private void configureApprovalGrid() {

        approvalGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS);

        approvalGrid.addColumn(
                AssigningApprovals::getLevel)
                .setHeader("Level");

        approvalGrid.addColumn(a ->
                a.getApprover() == null
                        ? ""
                        : a.getApprover().getEmployeeName())
                .setHeader("Approver");

        approvalGrid.addColumn(a ->
                a.getStatus() == null
                        ? ""
                        : a.getStatus().name())
                .setHeader("Status");

        approvalGrid.addComponentColumn(approval -> {

            Button deleteButton =
                    new Button("Delete");

            deleteButton.addClickListener(event -> {

                approvals.remove(approval);

                approvalGrid.getDataProvider()
                        .refreshAll();
            });

            return deleteButton;

        }).setHeader("Action");

        approvalGrid.setItems(approvals);

        approvalGrid.setWidthFull();

        approvalGrid.setHeight("250px");
    }

    // ================= ADD LINE =================

    private void addLine() {

        if (itemField.isEmpty()) {

            Notification.show(
                    "Please select item");

            return;
        }

        PurchaseRequestLine line =
                new PurchaseRequestLine();

        line.setItem(
                itemField.getValue());

        line.setQuantity(
                quantityField.getValue());

        line.setUnitPrice(
                unitPriceField.getValue());

        line.setDiscount(
                discountField.getValue());

        double quantity =
                quantityField.getValue() == null
                        ? 0
                        : quantityField.getValue();

        double unitPrice =
                unitPriceField.getValue() == null
                        ? 0
                        : unitPriceField.getValue();

        double discount =
                discountField.getValue() == null
                        ? 0
                        : discountField.getValue();

        double total =
                (quantity * unitPrice) - discount;

        line.setTotalPrice(total);

        lines.add(line);

        lineGrid.getDataProvider()
                .refreshAll();

        refreshTotal();

        itemField.clear();

        quantityField.setValue(1);

        unitPriceField.setValue(0.0);

        discountField.setValue(0.0);
    }

    // ================= ADD APPROVAL =================

    private void addApproval() {

        if (approverField.isEmpty()) {

            Notification.show(
                    "Please select approver");

            return;
        }

        AssigningApprovals approval =
                new AssigningApprovals();

        approval.setApprover(
                approverField.getValue());

        approval.setLevel(
                levelField.getValue());

        approval.setStatus(
                Status.WAITING_APPROVAL);

        approvals.add(approval);

        approvalGrid.getDataProvider()
                .refreshAll();

        approverField.clear();

        levelField.setValue(1);
    }

    // ================= REFRESH TOTAL =================

    private void refreshTotal() {

        double total =
                lines.stream()
                        .mapToDouble(
                                PurchaseRequestLine::getTotalPrice)
                        .sum();

        totalLabel.setText(
                String.valueOf(total));
    }

    // ================= SAVE =================

    private void savePurchaseRequest() {

        try {

            if (departmentField.isEmpty()) {

                Notification.show(
                        "Please select department");

                return;
            }

            if (lines.isEmpty()) {

                Notification.show(
                        "Please add minimum one line");

                return;
            }

            Users loggedInUser =
                    securityService.getLoggedInUser();

            Employee createdBy =
                    loggedInUser.getEmployee();

            PurchaseRequestHeader header =
                    new PurchaseRequestHeader();

            header.setCreatedBy(createdBy);

            header.setCreatedDate(
                    Date.valueOf(
                            createdDateField.getValue()));

            header.setForDepartment(
                    departmentField.getValue());

            header.setStatus(
                    Status.WAITING_APPROVAL);

            double total =
                    lines.stream()
                            .mapToDouble(
                                    PurchaseRequestLine::getTotalPrice)
                            .sum();

            header.setTotalAmount(total);

            for (PurchaseRequestLine line : lines) {

                line.setPurchaseRequestHeader(header);
            }

            header.setPurchaseRequestLines(lines);

            header.setAssigningApprovals(approvals);

            purchaseRequestHeaderService
                    .addPurchaseRequestHeader(header);

            Notification.show(
                    "Purchase Request Saved Successfully");

            lines.clear();

            approvals.clear();

            lineGrid.getDataProvider()
                    .refreshAll();

            approvalGrid.getDataProvider()
                    .refreshAll();

            refreshTotal();

        } catch (Exception exception) {

            Notification.show(
                    "Error : " + exception.getMessage());
        }
    }
}