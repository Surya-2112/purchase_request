package com.module.purchase.view.purchaseRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.entity.Users;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.ItemVariantService;
import com.module.purchase.service.PurchaseRequestDocumentService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.RepeatedPeriodService;
import com.module.purchase.service.NeedsService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
public class PurchaseRequestFormView extends VerticalLayout implements BeforeEnterObserver {

        private final PurchaseRequestHeaderService headerService;
        private final PurchaseRequestLineService lineService;
        private final PurchaseRequestDocumentService documentService;
        private final ItemVariantService itemVariantService;
        private final SecurityService securityService;
        private final RepeatedPeriodService repeatedPeriodService;
        private final NeedsService needsService;

        // ================= UI INPUT FIELDS =================
        private final ComboBox<Department> departmentField = new ComboBox<>("Department");
        private final ComboBox<Item> itemField = new ComboBox<>("Item");
        private final ComboBox<ItemVariant> variantField = new ComboBox<>("Specification");
        private final TextField unitField = new TextField("Unit");
        private final NumberField quantityField = new NumberField("Quantity");
        private final TextField descriptionField = new TextField("Description");

        // ================= DOCUMENTS, LINES & SCHEDULES COMPONENTS =================
        private final List<PurchaseRequestDocument> documents = new ArrayList<>();
        private final Grid<PurchaseRequestDocument> documentGrid = new Grid<>(PurchaseRequestDocument.class, false);
        private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        private final Upload upload = new Upload(buffer);

        private final List<PurchaseRequestLine> lines = new ArrayList<>();
        private PurchaseRequestHeader editingHeader = null;
        private PurchaseRequestLine editingLine = null;
        
        private final Grid<PurchaseRequestLine> lineGrid = new Grid<>(PurchaseRequestLine.class, false);
        private final VerticalLayout recurringScheduleSection = new VerticalLayout();
        private final Grid<PurchaseRequestLine> scheduleGrid = new Grid<>(PurchaseRequestLine.class, false);

        private final Button saveButton = new Button("Save & Go To Approval");

        private final Map<PurchaseRequestLine, RepeatedPeriod> pendingLineSchedulesMap = new HashMap<>();
        private RepeatedPeriod runtimeCachedSchedule = null;

        public PurchaseRequestFormView(
                        PurchaseRequestHeaderService headerService,
                        DepartmentService departmentService,
                        ItemService itemService,
                        ItemVariantService itemVariantService,
                        SecurityService securityService,
                        PurchaseRequestLineService lineService,
                        PurchaseRequestDocumentService documentService,
                        RepeatedPeriodService repeatedPeriodService,
                        NeedsService needsService
        ) {
                this.headerService = headerService;
                this.lineService = lineService;
                this.documentService = documentService;
                this.itemVariantService = itemVariantService;
                this.securityService = securityService;
                this.repeatedPeriodService = repeatedPeriodService;
                this.needsService = needsService;

                setSizeFull();
                setPadding(true);
                setSpacing(true);
                getStyle().set("overflow", "auto");

                departmentField.setItems(departmentService.getDepartments());
                departmentField.setItemLabelGenerator(Department::getDepartmentName);
                departmentField.setWidth("300px");

                // Execute custom access gating logic for departmental selections
                evaluateDepartmentAccessControl();

                itemField.setItems(itemService.getItems());
                itemField.setItemLabelGenerator(Item::getItemName);
                itemField.setWidth("250px");

                variantField.setWidth("250px");
                variantField.setItemLabelGenerator(variant -> variant.getSpecification() != null ? variant.getSpecification() : "Default / No Spec");

                itemField.addValueChangeListener(event -> {
                        Item selectedItem = event.getValue();
                        if (selectedItem != null) {
                                variantField.setItems(itemVariantService.getItemVariantsByItem(selectedItem));
                        } else {
                                variantField.clear();
                                variantField.setItems(new ArrayList<>());
                        }
                });

                variantField.addValueChangeListener(event -> {
                        ItemVariant variant = event.getValue();
                        if (variant != null && variant.getItem() != null && variant.getItem().getUnit() != null) {
                                unitField.setValue(variant.getItem().getUnit().getCode());
                        } else {
                                unitField.clear();
                        }
                });

                unitField.setReadOnly(true);
                unitField.setWidth("100px");

                quantityField.setValue(1.0);
                quantityField.setWidth("120px");

                descriptionField.setWidth("300px");

                configureGrid();
                configureScheduleGrid(); 
                configureDocumentGrid();
                configureUpload();

                Button addLineButton = new Button("Add / Update Line", e -> addLine());
                
                // Add Unlisted Item Pop-up Button Trigger
                Button addUnlistedItemBtn = new Button("Add Unlisted Item", e -> openUnlistedItemRequestFormModal());
                addUnlistedItemBtn.addThemeName("primary warning small");

                saveButton.addClickListener(e -> saveAndGoApproval());

                HorizontalLayout lineInput = new HorizontalLayout(
                                itemField,
                                variantField,
                                quantityField,
                                unitField,
                                descriptionField,
                                addLineButton
                );
                lineInput.setWidthFull();
                lineInput.setAlignItems(Alignment.END);
                lineInput.setFlexGrow(1, itemField);

                HorizontalLayout headerLayout = new HorizontalLayout(departmentField);
                headerLayout.setAlignItems(Alignment.END);
                headerLayout.setWidthFull();

                recurringScheduleSection.add(new H3("Active Recurring Sourcing Routines"), scheduleGrid);
                recurringScheduleSection.setPadding(false);
                recurringScheduleSection.setSpacing(true);
                recurringScheduleSection.setVisible(false); 

                VerticalLayout contentLayout = new VerticalLayout(
                                new H2("Purchase Request Form"),
                                headerLayout,
                                new H3("Purchase Request Lines"),
                                lineInput,
                        addUnlistedItemBtn,
                                lineGrid,
                                recurringScheduleSection, 
                                new H3("Upload Documents"),
                                upload,
                                documentGrid,
                                saveButton
                );
                contentLayout.setWidthFull();
                contentLayout.setSpacing(true);

                Scroller scroller = new Scroller(contentLayout);
                scroller.setSizeFull();
                add(scroller);
        }

        private void evaluateDepartmentAccessControl() {
                try {
                        Users activeUser = securityService.getLoggedInUser();
                        boolean scaleAccessAllowed = securityService.canAccessView("purchase-request-department");

                        if (!scaleAccessAllowed) {
                                departmentField.setReadOnly(true);
                                if (activeUser != null && activeUser.getEmployee() != null && activeUser.getEmployee().getDepartment() != null) {
                                        departmentField.setValue(activeUser.getEmployee().getDepartment());
                                }
                        }
                } catch (Exception ex) {
                        departmentField.setReadOnly(true);
                        ex.printStackTrace();
                }
        }

        private void openUnlistedItemRequestFormModal() {
                Dialog requestModal = new Dialog();
                requestModal.setHeaderTitle("Request Unlisted Material Specification");
                requestModal.setWidth("450px");

                TextField unlistedName = new TextField("Suggested Item Name");
                TextField unlistedSpec = new TextField("Detailed Specification (Size, GSM, etc.)");
                NumberField unlistedQty = new NumberField("Quantity Needed");
                
                unlistedName.setRequired(true);
                unlistedQty.setValue(1.0);
                unlistedQty.setMin(1.0);
                unlistedQty.setWidthFull();
                unlistedName.setWidthFull();
                unlistedSpec.setWidthFull();

                VerticalLayout modalFormLayout = new VerticalLayout(unlistedName, unlistedSpec, unlistedQty);
                modalFormLayout.setPadding(false);
                modalFormLayout.setSpacing(true);

                Button commitDraftBtn = new Button("Add to Request Drafts", VaadinIcon.FILE_ADD.create());
                commitDraftBtn.addThemeName("primary success");
                commitDraftBtn.addClickListener(clickEvent -> {
                        if (unlistedName.isEmpty() || unlistedQty.isEmpty() || unlistedQty.getValue() <= 0) {
                                Notification.show("Item Name and a valid Quantity are required.", 3000, Notification.Position.MIDDLE);
                                return;
                        }

                        String serializedNeedPayload = String.format("Name: %s | Spec: %s | Qty: %.2f", 
                                        unlistedName.getValue().trim(), 
                                        unlistedSpec.getValue().isEmpty() ? "No Spec Provided" : unlistedSpec.getValue().trim(),
                                        unlistedQty.getValue());

                        PurchaseRequestLine temporaryAdHocLine = new PurchaseRequestLine();
                        temporaryAdHocLine.setStatus(Status.DRAFT);
                        temporaryAdHocLine.setDescription( serializedNeedPayload);
                        temporaryAdHocLine.setRequestedQuantity(unlistedQty.getValue());

                        lines.add(temporaryAdHocLine);
                        refreshGridDataProviders();
                        
                        requestModal.close();
                        Notification.show("new item added in drafts.");
                });

                Button closeBtn = new Button("Discard", e -> requestModal.close());
                closeBtn.addThemeName("tertiary error");

                requestModal.getFooter().add(closeBtn, commitDraftBtn);
                requestModal.add(modalFormLayout);
                requestModal.open();
        }

        private void configureDocumentGrid() {
                documentGrid.removeAllColumns();
                documentGrid.addColumn(PurchaseRequestDocument::getFileName).setHeader("File Name").setAutoWidth(true);
                documentGrid.addColumn(PurchaseRequestDocument::getFileType).setHeader("Type").setAutoWidth(true);
                documentGrid.addColumn(document -> {
                        if (document.getFileSize() == null) return "0 KB";
                        return (document.getFileSize() / 1024) + " KB";
                }).setHeader("Size");

                documentGrid.addComponentColumn(document -> {
                        Button removeButton = new Button("Remove");
                        removeButton.addClickListener(event -> {
                                try {
                                        if (document.getDocumentId() != null) {
                                                documentService.delete(document);
                                        }
                                        documents.remove(document);
                                        documentGrid.getDataProvider().refreshAll();
                                        if (documents.isEmpty()) {
                                                documentGrid.setVisible(false);
                                        }
                                        Notification.show("Document removed");
                                } catch (Exception ex) {
                                        ex.printStackTrace();
                                        Notification.show("Failed to remove document");
                                }
                        });
                        return removeButton;
                }).setHeader("Action");

                documentGrid.setItems(documents);
                documentGrid.setWidthFull();
                documentGrid.setAllRowsVisible(true);
                documentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                documentGrid.setVisible(false);
        }

        private void configureUpload() {
                upload.setWidthFull();
                upload.setMaxFiles(10);
                upload.setDropLabel(new Span("Drop files here or click to upload"));
                upload.setAcceptedFileTypes(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".png", ".jpg", ".jpeg");

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
                                documentGrid.setVisible(true);
                                documentGrid.getDataProvider().refreshAll();

                                Notification.show(fileName + " uploaded successfully");
                        } catch (IOException exception) {
                                Notification.show("File upload failed");
                        }
                });
        }

        @Override
        public void beforeEnter(BeforeEnterEvent event) {
                Optional<String> parameter = event.getRouteParameters().get("id");
                if (parameter.isPresent()) {
                        Long id = Long.parseLong(parameter.get());
                        editingHeader = headerService.getPurchaseRequestHeaderById(id).orElse(null);

                        if (editingHeader == null) {
                                Notification.show("Purchase Request Not Found");
                                return;
                        }
                        departmentField.setReadOnly(true); 
                        loadEditData();
                }
        }

        private void loadEditData() {
                departmentField.setValue(editingHeader.getForDepartment());

                lines.clear();
                pendingLineSchedulesMap.clear();
                
                List<PurchaseRequestLine> dbLines = lineService.getPurchaseRequestLineByHeader(editingHeader);
                for (PurchaseRequestLine line : dbLines) {
                        lines.add(line);
                        if (line.getRepeatableId() != null) {
                                repeatedPeriodService.getRepeatedPeriodById(line.getRepeatableId())
                                        .ifPresent(period -> pendingLineSchedulesMap.put(line, period));
                        }
                }
                
                refreshGridDataProviders();
                saveButton.setText("Update & Go To Approval");
        }

        private void configureGrid() {
                lineGrid.removeAllColumns();

                lineGrid.addColumn(line -> {
                                if (line.getDescription() != null && line.getDescription().contains(" [UNLISTED CATALOG ITEM]")) {
                                        return " Custom Ad-Hoc Request";
                                }
                                return line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                                ? line.getItemVariant().getItem().getItemName() : "";
                        })
                        .setHeader("Item").setAutoWidth(true);

                lineGrid.addColumn(line -> {
                                if (line.getDescription() != null && line.getDescription().contains("⚠️ [UNLISTED CATALOG ITEM]")) {
                                        return "See Raw Parameters Log Below";
                                }
                                return line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "";
                        })
                        .setHeader("Specification").setAutoWidth(true);

                lineGrid.addColumn(PurchaseRequestLine::getRequestedQuantity).setHeader("Quantity").setWidth("120px");

                lineGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                                && line.getItemVariant().getItem().getUnit() != null 
                                ? line.getItemVariant().getItem().getUnit().getCode() : "-")
                                .setHeader("Unit").setWidth("100px");

                lineGrid.addColumn(PurchaseRequestLine::getDescription).setHeader("Description Details / Specifications").setAutoWidth(true);

                lineGrid.addComponentColumn(line -> {
                        HorizontalLayout rowContextActions = new HorizontalLayout();

                        if (line.getItemVariant() == null) {
                                 Button deleteButton = new Button("Delete");
                                deleteButton.addThemeName("small error");
                                deleteButton.addClickListener(e -> {
                                        lines.remove(line);
                                        pendingLineSchedulesMap.remove(line);
                                        refreshGridDataProviders();
                                        Notification.show("Line item deleted.");
                                });
                                        rowContextActions.add(deleteButton);
                                } else {
                                Button editButton = new Button("Edit");
                                editButton.addThemeName("small primary warning");
                                editButton.addClickListener(e -> editLine(line));

                                Button deleteButton = new Button("Delete");
                                deleteButton.addThemeName("small error");
                                deleteButton.addClickListener(e -> {
                                        lines.remove(line);
                                        pendingLineSchedulesMap.remove(line);
                                        refreshGridDataProviders();
                                        Notification.show("Line item deleted.");
                                });
                                rowContextActions.add(editButton, deleteButton);
                        }
                        return rowContextActions;
                }).setHeader("Actions / Governance").setWidth("280px");

                lineGrid.setItems(lines);
                lineGrid.setWidthFull();
                lineGrid.setAllRowsVisible(true);
                lineGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        }

        private void configureScheduleGrid() {
                scheduleGrid.removeAllColumns();

                scheduleGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                ? line.getItemVariant().getItem().getItemName() : "")
                                .setHeader("Scheduled Item").setAutoWidth(true);

                scheduleGrid.addColumn(line -> {
                        RepeatedPeriod p = pendingLineSchedulesMap.get(line);
                        if (p == null) return "No Schedule Mapped";
                        return "Every " + p.getFrequencyPeriod() + " " + (p.getFrequencyType() != null ? p.getFrequencyType().name() : "");
                }).setHeader("Recurrence Interval").setAutoWidth(true);

                scheduleGrid.addColumn(line -> {
                        RepeatedPeriod p = pendingLineSchedulesMap.get(line);
                        return (p != null && p.getFromDate() != null) ? p.getFromDate().toString() : "-";
                }).setHeader("Start Date").setWidth("140px");

                scheduleGrid.addColumn(line -> {
                        RepeatedPeriod p = pendingLineSchedulesMap.get(line);
                        return (p != null && p.getToDate() != null) ? p.getToDate().toString() : "Indefinite";
                }).setHeader("End Date").setWidth("140px");

                scheduleGrid.addComponentColumn(line -> {
                        Button modifyScheduleBtn = new Button("Modify Schedule", e -> {
                                AutoRfqScheduleDialog dialog = new AutoRfqScheduleDialog(updatedPeriod -> {
                                        pendingLineSchedulesMap.put(line, updatedPeriod);
                                        refreshGridDataProviders();
                                        Notification.show("Repetition parameters updated.");
                                });
                                dialog.open();
                        });
                        
                        Button clearScheduleBtn = new Button("Remove Loop", e -> {
                                pendingLineSchedulesMap.remove(line);
                                line.setRepeatableId(null);
                                refreshGridDataProviders();
                                Notification.show("Recurrence cycle detached from this item.");
                        });
                        clearScheduleBtn.addThemeName("error layout");

                        return new HorizontalLayout(modifyScheduleBtn, clearScheduleBtn);
                }).setHeader("Scheduling Maintenance").setWidth("320px");

                scheduleGrid.setWidthFull();
                scheduleGrid.setAllRowsVisible(true);
                scheduleGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        }

        private void refreshGridDataProviders() {
                lineGrid.getDataProvider().refreshAll();
                List<PurchaseRequestLine> repeatableLines = lines.stream()
                                .filter(pendingLineSchedulesMap::containsKey)
                                .toList();
                
                scheduleGrid.setItems(repeatableLines);
                scheduleGrid.getDataProvider().refreshAll();
                recurringScheduleSection.setVisible(!repeatableLines.isEmpty());
        }

        private void addLine() {
                if (itemField.isEmpty()) {
                        Notification.show("Please select an item");
                        return;
                }
                if(variantField.isEmpty()) {
                        Notification.show("Please select a specification");
                        return;
                }
                if (quantityField.isEmpty() || quantityField.getValue() <= 0) {
                        Notification.show("Quantity must be greater than 0");
                        return;
                }

                ItemVariant selectedVariant = variantField.getValue();
                double enteredQuantity = quantityField.getValue();
                String enteredDescription = descriptionField.getValue() != null ? descriptionField.getValue() : "";

                if (editingLine == null && selectedVariant.getItem() != null && 
                    selectedVariant.getItem().getCategory() != null && 
                    selectedVariant.getItem().getCategory().isRepeatable()) {

                        ConfirmDialog confirmDialog = new ConfirmDialog();
                        confirmDialog.setHeader("Repeatable Category Detected");
                        confirmDialog.setText("This item belongs to a repeatable category. Do you want to configure a recurring schedule for this line item?");
                        
                        confirmDialog.setCancelable(true);
                        confirmDialog.setCancelText("No, Single Order");
                        confirmDialog.setConfirmText("Yes, Setup Schedule");

                        confirmDialog.addConfirmListener(event -> {
                                AutoRfqScheduleDialog scheduleDialog = new AutoRfqScheduleDialog(generatedPeriod -> {
                                        this.runtimeCachedSchedule = generatedPeriod;
                                        commitLineItemData(selectedVariant, enteredQuantity, enteredDescription);
                                });
                                scheduleDialog.open();
                        });

                        confirmDialog.addCancelListener(event -> {
                                this.runtimeCachedSchedule = null;
                                commitLineItemData(selectedVariant, enteredQuantity, enteredDescription);
                        });

                        confirmDialog.open();
                } else {
                        commitLineItemData(selectedVariant, enteredQuantity, enteredDescription);
                }
        }

        private void commitLineItemData(ItemVariant selectedVariant, double enteredQuantity, String enteredDescription) {
                if (editingLine != null) {
                        editingLine.setItemVariant(selectedVariant);
                        editingLine.setRequestedQuantity(enteredQuantity);
                        editingLine.setDescription(enteredDescription);
                        
                        if (this.runtimeCachedSchedule != null) {
                                pendingLineSchedulesMap.put(editingLine, this.runtimeCachedSchedule);
                        } else {
                                pendingLineSchedulesMap.remove(editingLine);
                                editingLine.setRepeatableId(null);
                        }
                        
                        Notification.show("Line updated");
                        editingLine = null;
                } else {
                        Optional<PurchaseRequestLine> existingLineOpt = lines.stream()
                                        .filter(line -> line.getItemVariant() != null && 
                                                        line.getItemVariant().getId().equals(selectedVariant.getId()))
                                        .findFirst();

                        PurchaseRequestLine targetedLine;
                        if (existingLineOpt.isPresent()) {
                                targetedLine = existingLineOpt.get();
                                targetedLine.setRequestedQuantity(targetedLine.getRequestedQuantity() + enteredQuantity);
                                
                                if (!enteredDescription.isEmpty()) {
                                        if (targetedLine.getDescription() == null || targetedLine.getDescription().isEmpty()) {
                                                targetedLine.setDescription(enteredDescription);
                                        } else if (!targetedLine.getDescription().contains(enteredDescription)) {
                                                targetedLine.setDescription(targetedLine.getDescription() + " | " + enteredDescription);
                                        }
                                }
                                Notification.show("Quantity updated for existing line item");
                        } else {
                                targetedLine = new PurchaseRequestLine();
                                targetedLine.setItemVariant(selectedVariant);
                                targetedLine.setRequestedQuantity(enteredQuantity);
                                targetedLine.setDescription(enteredDescription);
                                targetedLine.setStatus(Status.DRAFT);
                                lines.add(targetedLine);
                                Notification.show("Line added");
                        }

                        if (this.runtimeCachedSchedule != null) {
                                pendingLineSchedulesMap.put(targetedLine, this.runtimeCachedSchedule);
                        }
                }

                refreshGridDataProviders();
                clearLine();
        }

        private void editLine(PurchaseRequestLine line) {
                editingLine = line;
                if (line.getItemVariant() != null) {
                        itemField.setValue(line.getItemVariant().getItem());
                        variantField.setValue(line.getItemVariant());
                }
                quantityField.setValue(line.getRequestedQuantity());
                descriptionField.setValue(line.getDescription() != null ? line.getDescription() : "");
                
                this.runtimeCachedSchedule = pendingLineSchedulesMap.get(line);
                Notification.show("Edit mode enabled");
        }

        private void clearLine() {
                itemField.clear();
                variantField.clear();
                unitField.clear();
                quantityField.setValue(1.0);
                descriptionField.clear();
                editingLine = null;
                this.runtimeCachedSchedule = null;
        }

        private void saveAndGoApproval() {
        if (departmentField.isEmpty() || lines.isEmpty()) {
                Notification.show("Department and at least one Line are required");
                return;
        }

        try {
                Employee currentUser = securityService.getLoggedInUser().getEmployee();
                PurchaseRequestHeader savedHeader;
                
                double calculatedGlobalTotal = 0.0;
                for (PurchaseRequestLine prLine : lines) {
                        if (prLine.getItemVariant() != null && prLine.getItemVariant().getEstimatedUnitPrice() != null) {
                                double unitPrice = prLine.getItemVariant().getEstimatedUnitPrice();
                                double requestedQty = prLine.getRequestedQuantity() != null ? prLine.getRequestedQuantity() : 0.0;
                                calculatedGlobalTotal += (unitPrice * requestedQty);
                        }
                }

                if (editingHeader != null) {
                        editingHeader.setForDepartment(departmentField.getValue());
                        editingHeader.setTotalAmount(calculatedGlobalTotal); 
                        savedHeader = headerService.updatePurchaseRequestHeader(editingHeader, currentUser);
                        
                        lineService.deleteAllLine(savedHeader);
                        needsService.resolveAndClearCompletedNeed(com.module.purchase.enums.EntityType.ITEM, savedHeader.getPurchaseRequestId());
                } else {
                        PurchaseRequestHeader header = new PurchaseRequestHeader();
                        header.setCreatedDate(new java.sql.Date(System.currentTimeMillis()));
                        header.setForDepartment(departmentField.getValue());
                        header.setStatus(Status.DRAFT); // Parent document saved as DRAFT context
                        header.setCreatedBy(currentUser);
                        header.setLevel(1);
                        header.setTotalAmount(calculatedGlobalTotal); 
                        savedHeader = headerService.addPurchaseRequestHeader(header, currentUser);
                }

                // 3. Split processing loop for the memory data array
                for (PurchaseRequestLine memoryLine : lines) {
                        
                        // CASE A: The item is unlisted (No ItemVariant attached)
                        if (memoryLine.getItemVariant() == null && memoryLine.getDescription() != null) {
                                String cleanPayloadText = memoryLine.getDescription().replace("⚠️ [UNLISTED CATALOG ITEM] - ", "");
                                
                                // Store directly inside the generic Needs table, linking it to the Header ID!
                                needsService.registerNewCatalogNeed(
                                        cleanPayloadText, 
                                        com.module.purchase.enums.EntityType.ITEM, 
                                        savedHeader.getPurchaseRequestId() // Tied to Header ID directly
                                );
                        } 
                        // CASE B: Standard verified catalog material SKU entry
                        else if (memoryLine.getItemVariant() != null) {
                                PurchaseRequestLine dbLine = new PurchaseRequestLine();
                                dbLine.setPurchaseRequestHeader(savedHeader);
                                dbLine.setItemVariant(memoryLine.getItemVariant());
                                dbLine.setRequestedQuantity(memoryLine.getRequestedQuantity());
                                dbLine.setDescription(memoryLine.getDescription());
                                dbLine.setStatus(Status.DRAFT);
                                
                                double unitPrice = (memoryLine.getItemVariant().getEstimatedUnitPrice() != null) 
                                                ? memoryLine.getItemVariant().getEstimatedUnitPrice() : 0.0;
                                dbLine.setItemUnitPrice(unitPrice);
                                dbLine.setItemTotalAmount(unitPrice * memoryLine.getRequestedQuantity());

                                dbLine = lineService.addPurchaseRequestLine(dbLine);

                                // Manage recurring execution tracks if applicable
                                if (pendingLineSchedulesMap.containsKey(memoryLine)) {
                                        RepeatedPeriod rawSchedule = pendingLineSchedulesMap.get(memoryLine);
                                        rawSchedule.setReferType(RepeatedPeriodReferType.PURCHASE_REQUEST_LINE);
                                        rawSchedule.setReferId(dbLine.getId());     
                                        RepeatedPeriod savedSchedule = repeatedPeriodService.addRepeatedPeriod(rawSchedule, currentUser);
                                        dbLine.setRepeatableId(savedSchedule.getId());
                                        lineService.updatePurchaseRequestLine(dbLine);
                                }
                        }
                }

                // 4. Save attached support metadata files logs
                for (PurchaseRequestDocument document : documents) {
                        document.setPurchaseRequestHeader(savedHeader);
                        documentService.save(document);
                }

                Notification.show("Purchase Request draft and custom unlisted requirements saved successfully.");
                getUI().ifPresent(ui -> ui.navigate("purchase-request-approval/" + savedHeader.getPurchaseRequestId()));

        } catch (Exception exception) {
                exception.printStackTrace();
                Notification.show("Error: " + exception.getMessage(), 5000, Notification.Position.MIDDLE);
        }
}
}