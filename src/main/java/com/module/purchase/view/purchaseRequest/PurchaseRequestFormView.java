package com.module.purchase.view.purchaseRequest;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.Status;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.PurchaseRequestDocumentService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.VendorService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-form/:id?", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestFormView extends VerticalLayout
                implements BeforeEnterObserver {

        private final PurchaseRequestHeaderService headerService;

        private final PurchaseRequestLineService lineService;

        private final PurchaseRequestDocumentService documentService;

        private final SecurityService securityService;

        private final DatePicker createdDateField = new DatePicker("Created Date");

        private final ComboBox<Department> departmentField = new ComboBox<>("Department");

        private final ComboBox<Vendor> vendorField = new ComboBox<>("Vendor");

        private final ComboBox<Item> itemField = new ComboBox<>("Item");

        private final IntegerField quantityField = new IntegerField("Quantity");

        private final NumberField unitPriceField = new NumberField("Unit Price");

        private final NumberField discountField = new NumberField("Discount");

        private final TextField VATCodeField = new TextField("VAT Code");

        private final List<PurchaseRequestDocument> documents = new ArrayList<>();

        private final Grid<PurchaseRequestDocument> documentGrid = new Grid<>(PurchaseRequestDocument.class, false);

        private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();

        private final Upload upload = new Upload(buffer);

        private final List<PurchaseRequestLine> lines = new ArrayList<>();

        private PurchaseRequestHeader editingHeader = null;

        private PurchaseRequestLine editingLine = null;

        private final Grid<PurchaseRequestLine> lineGrid = new Grid<>(PurchaseRequestLine.class, false);

        private final Button saveButton = new Button("Save & Go To Approval");

        public PurchaseRequestFormView(

                        PurchaseRequestHeaderService headerService,
                        DepartmentService departmentService,
                        ItemService itemService,
                        SecurityService securityService,
                        PurchaseRequestLineService lineService,
                        VendorService vendorService,
                        PurchaseRequestDocumentService documentService

        ) {

                this.headerService = headerService;

                this.securityService = securityService;

                this.lineService = lineService;

                this.documentService = documentService;

                // ================= MAIN VIEW =================

                setSizeFull();

                setPadding(true);

                setSpacing(true);

                getStyle().set("overflow", "auto");


                departmentField.setItems(departmentService.getDepartments());

                departmentField.setItemLabelGenerator(Department::getDepartmentName);

                departmentField.setWidth("300px");


                vendorField.setItems(vendorService.getVendors());

                vendorField.setItemLabelGenerator(Vendor::getVendorName);

                vendorField.setWidth("300px");

                // ================= ITEM =================

                itemField.setItems(itemService.getItems());

                itemField.setItemLabelGenerator(Item::getItemName);

                itemField.setWidth("250px");

                // ================= DEFAULT VALUES =================

                createdDateField.setValue(LocalDate.now());

                quantityField.setValue(1);

                quantityField.setWidth("120px");

                discountField.setValue(0.0);

                discountField.setWidth("120px");

                unitPriceField.setWidth("150px");

                VATCodeField.setWidth("150px");

                VATCodeField.setReadOnly(true);

                itemField.addValueChangeListener(event -> {

                        Item item = event.getValue();

                        unitPriceField.setValue(item == null? 0.0: item.getUnitPrice());

                        VATCodeField.setValue( item == null? "" : item.getVATCode());
                });

                configureGrid();

                configureDocumentGrid();

                configureUpload();

                Button addLineButton = new Button(
                                "Add / Update Line",
                                e -> addLine());

                saveButton.addClickListener(
                                e -> saveAndGoApproval());

                HorizontalLayout lineInput = new HorizontalLayout(

                                itemField,

                                quantityField,

                                unitPriceField,

                                VATCodeField,

                                discountField,

                                addLineButton);

                lineInput.setWidthFull();

                lineInput.setAlignItems(
                                Alignment.END);

                lineInput.setFlexGrow(1, itemField);

                HorizontalLayout headerLayout = new HorizontalLayout(departmentField, vendorField);

                headerLayout.setWidthFull();

                // ================= CONTENT =================

                VerticalLayout contentLayout = new VerticalLayout(

                                new H2("Purchase Request Form"),

                                createdDateField,

                                headerLayout,

                                new H3("Purchase Request Lines"),

                                lineInput,

                                lineGrid,

                                new H3("Upload Documents"),

                                upload,

                                documentGrid,

                                saveButton);

                contentLayout.setWidthFull();

                contentLayout.setSpacing(true);

                // ================= SCROLLER =================

                Scroller scroller = new Scroller(contentLayout);

                scroller.setSizeFull();

                add(scroller);
        }

        // ================= DOCUMENT GRID =================

        private void configureDocumentGrid() {

                documentGrid.removeAllColumns();

                documentGrid.addColumn(
                                PurchaseRequestDocument::getFileName)
                                .setHeader("File Name")
                                .setAutoWidth(true);

                documentGrid.addColumn(
                                PurchaseRequestDocument::getFileType)
                                .setHeader("Type")
                                .setAutoWidth(true);

                documentGrid.addColumn(document -> {

                        if (document.getFileSize() == null) {
                                return "0 KB";
                        }

                        return (document.getFileSize() / 1024) + " KB";

                }).setHeader("Size");

                documentGrid.addComponentColumn(document -> {

                        Button removeButton = new Button("Remove");

                        removeButton.addClickListener(event -> {

                                try {

                                        // REMOVE FROM DATABASE IF SAVED
                                        if (document.getDocumentId() != null) {

                                                documentService.delete(document);
                                        }

                                        // REMOVE FROM UI LIST
                                        documents.remove(document);

                                        documentGrid.getDataProvider()
                                                        .refreshAll();

                                        if (documents.isEmpty()) {

                                                documentGrid.setVisible(false);
                                        }

                                        Notification.show(
                                                        "Document removed");

                                } catch (Exception ex) {

                                        ex.printStackTrace();

                                        Notification.show(
                                                        "Failed to remove document");
                                }
                        });

                        return removeButton;

                }).setHeader("Action");

                documentGrid.setItems(documents);

                documentGrid.setWidthFull();

                documentGrid.setAllRowsVisible(true);

                documentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

                // Hide initially
                documentGrid.setVisible(false);
        }

        // ================= UPLOAD =================

        private void configureUpload() {

                upload.setWidthFull();

                upload.setMaxFiles(10);

                upload.setDropLabel(new com.vaadin.flow.component.html.Span("Drop files here or click to upload"));

                upload.setAcceptedFileTypes(

                                ".pdf",
                                ".doc",
                                ".docx",
                                ".xls",
                                ".xlsx",
                                ".png",
                                ".jpg",
                                ".jpeg");

                upload.addSucceededListener(event -> {

                        try {

                                String fileName = event.getFileName();

                                byte[] data = buffer.getInputStream(fileName).readAllBytes();

                                PurchaseRequestDocument document = new PurchaseRequestDocument();

                                document.setFileName(fileName);

                                document.setFileType(event.getMIMEType());

                                document.setFileSize((long) data.length);

                                document.setDocumentData(data);

                                documents.add(document);

                                // SHOW GRID ONLY AFTER FILE ADDED
                                documentGrid.setVisible(true);

                                documentGrid.getDataProvider()
                                                .refreshAll();

                                Notification.show(fileName + " uploaded successfully");

                        } catch (IOException exception) {

                                Notification.show(
                                                "File upload failed");
                        }
                });
        }

        // ================= ROUTE =================

        @Override
        public void beforeEnter(BeforeEnterEvent event) {

                Optional<String> parameter = event.getRouteParameters().get("id");

                if (parameter.isPresent()) {

                        Long id = Long.parseLong(parameter.get());

                        editingHeader = headerService
                                        .getPurchaseRequestHeaderById(id)
                                        .orElse(null);

                        if (editingHeader == null) {

                                Notification.show( "Purchase Request Not Found");
                                return;
                        }

                        loadEditData();
                }
        }

        private void loadEditData() {

                createdDateField.setValue(

                                editingHeader
                                                .getCreatedDate()
                                                .toLocalDate());

                departmentField.setValue(
                                editingHeader.getForDepartment());

                vendorField.setValue(
                                editingHeader.getVendor());

                lines.clear();

                lines.addAll(

                                lineService
                                                .getPurchaseRequestLineByHeader(
                                                                editingHeader));

                lineGrid.getDataProvider()
                                .refreshAll();

                documents.clear();

                documents.addAll(documentService.getByPurchaseRequestHeader(editingHeader));

                documentGrid.setVisible(!documents.isEmpty());

                documentGrid.getDataProvider().refreshAll();
                saveButton.setText(
                                "Update & Go To Approval");
        }

        // ================= LINE GRID =================

        private void configureGrid() {

                lineGrid.removeAllColumns();

                lineGrid.addColumn(line ->

                line.getItem() != null

                                ? line.getItem()
                                                .getItemName()

                                : "")

                                .setHeader("Item")
                                .setWidth("120px")
                                .setFlexGrow(1);

                lineGrid.addColumn(
                                PurchaseRequestLine::getQuantity)
                                .setHeader("Quantity")
                                .setWidth("120px");

                lineGrid.addColumn(
                                PurchaseRequestLine::getUnitPrice)
                                .setHeader("Unit Price")
                                .setWidth("150px");

                lineGrid.addColumn(line ->

                line.getItem() == null

                                ? ""

                                : line.getItem().getVATCode()

                ).setHeader("VAT Code")
                                .setWidth("140px");

                lineGrid.addColumn(
                                PurchaseRequestLine::getDiscount)
                                .setHeader("Discount")
                                .setWidth("130px");

                lineGrid.addColumn(
                                PurchaseRequestLine::getTotalPrice)
                                .setHeader("Total")
                                .setWidth("150px");

                lineGrid.addComponentColumn(line -> {

                        Button editButton = new Button("Edit");

                        editButton.addClickListener(
                                        e -> editLine(line));

                        Button deleteButton = new Button("Delete");

                        deleteButton.addClickListener(e -> {

                                lines.remove(line);

                                lineGrid.getDataProvider()
                                                .refreshAll();

                                Notification.show("Line deleted");
                        });

                        return new HorizontalLayout(
                                        editButton,
                                        deleteButton);

                }).setHeader("Actions")
                                .setWidth("220px");

                lineGrid.setItems(lines);

                lineGrid.setWidthFull();

                lineGrid.setAllRowsVisible(true);

                lineGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        }


        private void addLine() {

                if (itemField.isEmpty()) {

                        Notification.show(
                                        "Please select item");

                        return;
                }

                if(quantityField.getValue()<=0)
                {   Notification.show(" Quantity must be greater than 0");
                        return;
                }
                if(unitPriceField.getValue()<=0)
                {   Notification.show(" Unit price must be greater than 0");
                        return;
                }
                if(discountField.getValue()<0)
                {Notification.show("Discount must be postive");
                        return;
                }

                Integer qtyValue = quantityField.getValue();

                Double priceValue = unitPriceField.getValue();

                Double discountValue = discountField.getValue();

                int qty = qtyValue == null ? 0 : qtyValue;

                double price = priceValue == null ? 0 : priceValue;

                double discount = discountValue == null ? 0 : discountValue;

                double total = (qty * price) - discount;

                // ================= UPDATE =================

                if (editingLine != null) {

                        editingLine.setItem(
                                        itemField.getValue());

                        editingLine.setQuantity(qty);

                        editingLine.setUnitPrice(price);

                        editingLine.setDiscount(discount);

                        editingLine.setTotalPrice(total);

                        Notification.show(
                                        "Line updated");

                        editingLine = null;

                } else {

                        PurchaseRequestLine existingLine = null;

                        for (PurchaseRequestLine line : lines) {

                                if (line.getItem() != null && line.getItem().getItemId()
                                                .equals(itemField.getValue().getItemId())) {

                                        existingLine = line;

                                        break;
                                }
                        }

                        if (existingLine != null) {

                                int newQty = existingLine.getQuantity() + qty;

                                existingLine.setQuantity(newQty);

                                existingLine.setUnitPrice(price);

                                existingLine.setDiscount(
                                                existingLine.getDiscount() + discount);

                                double updatedTotal = (newQty * price)
                                                - existingLine.getDiscount();

                                existingLine.setTotalPrice(updatedTotal);

                                Notification.show(
                                                "Existing item quantity updated");

                        } else {

                                PurchaseRequestLine line = new PurchaseRequestLine();

                                line.setItem(
                                                itemField.getValue());

                                line.setQuantity(qty);

                                line.setUnitPrice(price);

                                line.setDiscount(discount);

                                line.setTotalPrice(total);

                                lines.add(line);

                                Notification.show(
                                                "Line added");
                        }
                }

                lineGrid.getDataProvider() .refreshAll();

                clearLine();
        }

        // ================= EDIT =================

        private void editLine(
                        PurchaseRequestLine line) {

                editingLine = line;

                itemField.setValue( line.getItem());

                quantityField.setValue(line.getQuantity());

                unitPriceField.setValue(line.getUnitPrice());

                discountField.setValue(line.getDiscount());

                Notification.show(
                                "Edit mode enabled");
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
                                || vendorField.isEmpty()
                                || lines.isEmpty()) {

                        Notification.show(
                                        "Department, Vendor and Lines required");

                        return;
                }

                try {

                        Employee currentUser =

                                        securityService
                                                        .getLoggedInUser()
                                                        .getEmployee();

                        double total = lines.stream()

                                        .mapToDouble(
                                                        PurchaseRequestLine::getTotalPrice)

                                        .sum();

                        PurchaseRequestHeader saved;

                        // ================= UPDATE =================

                        if (editingHeader != null) {

                                editingHeader.setCreatedDate(

                                                Date.valueOf(
                                                                createdDateField.getValue()));

                                editingHeader.setForDepartment(
                                                departmentField.getValue());

                                editingHeader.setVendor(
                                                vendorField.getValue());

                                editingHeader.setTotalAmount(total);

                                saved = headerService.updatePurchaseRequestHeader(
                                                editingHeader,
                                                currentUser);

                                // ================= DELETE OLD LINES =================

                                lineService.deleteAllLine(saved);

                                // ================= SAVE NEW LINES =================

                                for (PurchaseRequestLine oldLine : lines) {

                                        PurchaseRequestLine newLine = new PurchaseRequestLine();

                                        newLine.setPurchaseRequestHeader(saved);

                                        newLine.setItem(
                                                        oldLine.getItem());

                                        newLine.setQuantity(
                                                        oldLine.getQuantity());

                                        newLine.setUnitPrice(
                                                        oldLine.getUnitPrice());

                                        newLine.setDiscount(
                                                        oldLine.getDiscount());

                                        newLine.setTotalPrice(
                                                        oldLine.getTotalPrice());

                                        lineService.addPurchaseRequestLine(
                                                        newLine);
                                }

                                Notification.show(
                                                "Purchase Request Updated");

                        } else {

                                // ================= NEW SAVE =================

                                PurchaseRequestHeader header = new PurchaseRequestHeader();

                                header.setCreatedDate(

                                                Date.valueOf(
                                                                createdDateField.getValue()));

                                header.setForDepartment(
                                                departmentField.getValue());

                                header.setVendor(
                                                vendorField.getValue());

                                header.setStatus(Status.DRAFT);

                                header.setCreatedBy(currentUser);

                                header.setTotalAmount(total);

                                saved = headerService.addPurchaseRequestHeader(
                                                header,
                                                currentUser);

                                for (PurchaseRequestLine oldLine : lines) {

                                        PurchaseRequestLine newLine = new PurchaseRequestLine();

                                        newLine.setPurchaseRequestHeader(saved);

                                        newLine.setItem(
                                                        oldLine.getItem());

                                        newLine.setQuantity(
                                                        oldLine.getQuantity());

                                        newLine.setUnitPrice(
                                                        oldLine.getUnitPrice());

                                        newLine.setDiscount(
                                                        oldLine.getDiscount());

                                        newLine.setTotalPrice(
                                                        oldLine.getTotalPrice());

                                        lineService.addPurchaseRequestLine(
                                                        newLine);
                                }

                                Notification.show(
                                                "Purchase Request Saved");
                        }

                        for (PurchaseRequestDocument document : documents) {

                                document.setPurchaseRequestHeader(saved);
                                documentService.save(document);
                        }

                        Notification.show(
                                        "Documents Saved : " + documents.size());

                        getUI().ifPresent(ui ->

                        ui.navigate(

                                        "purchase-request-approval/"
                                                        + saved.getPurchaseRequestId()));

                } catch (Exception exception) {

                        exception.printStackTrace();

                        Notification.show(
                                        "Error : " + exception.getMessage(),
                                        5000,
                                        Notification.Position.MIDDLE);
                }
        }
}