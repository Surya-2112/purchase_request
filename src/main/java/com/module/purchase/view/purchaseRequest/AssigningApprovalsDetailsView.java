package com.module.purchase.view.purchaseRequest;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseRequestDocumentService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import jakarta.annotation.security.PermitAll;

@Route(value = "assigning-approvals-details/:id", layout = MainLayout.class)
@PermitAll
public class AssigningApprovalsDetailsView extends VerticalLayout implements BeforeEnterObserver {

        private final PurchaseRequestHeaderService headerService;
        private final PurchaseRequestLineService lineService;
        private final AssigningApprovalsService approvalsService;
        private final DepartmentBudgetService departmentBudgetService;
        private final PurchaseRequestDocumentService documentService;
        private final SecurityService securityService;

        private PurchaseRequestHeader header;
        private AssigningApprovals approval;

        // Header Metrics Labels
        private final Span requestId = new Span();
        private final Span createdBy = new Span();
        private final Span department = new Span();
        private final Span requestedTotalAmount = new Span();
        private final Span approvedTotalAmount = new Span(); 
        private final Span createdDate = new Span();
        private final Span level = new Span();
        private final Span status = new Span();

        // Budget Metrics Labels
        private final H3 budgetTitle = new H3();
        private final Span budgetYear = new Span();
        private final Span totalBudgetAmount = new Span();
        private final Span remainingBudgetAmount = new Span();
        private Double remainingBudget = 0.0;
        private boolean isBudgetConfigured = false; 

        private final Button approveBtn = new Button("Approve");
        private final Button rejectBtn = new Button("Reject");
        private final TextArea comments = new TextArea("Approver Comments / Remarks");

        // UI grids
        private final Grid<PurchaseRequestLine> lineGrid = new Grid<>(PurchaseRequestLine.class, false);
        private final Grid<PurchaseRequestDocument> documentGrid = new Grid<>(PurchaseRequestDocument.class, false);

        private final List<PurchaseRequestLine> workingLinesList = new ArrayList<>();

        public AssigningApprovalsDetailsView(
                        PurchaseRequestHeaderService headerService,
                        PurchaseRequestLineService lineService,
                        AssigningApprovalsService approvalsService,
                        DepartmentBudgetService departmentBudgetService,
                        PurchaseRequestDocumentService documentService,
                        SecurityService securityService) {

                this.headerService = headerService;
                this.lineService = lineService;
                this.approvalsService = approvalsService;
                this.departmentBudgetService = departmentBudgetService;
                this.documentService = documentService;
                this.securityService = securityService;

                setSizeFull();
                setPadding(false);
                setSpacing(false);

                comments.setWidthFull();
                comments.setMinHeight("100px");
                comments.setPlaceholder("Enter reasons for partial approval modifications or rejections here...");

                configureLineGrid();
                configureDocumentGrid();

                approveBtn.addThemeName("primary success");
                rejectBtn.addThemeName("primary error");

                approveBtn.addClickListener(e -> approveRequest());
                rejectBtn.addClickListener(e -> rejectRequest());

                HorizontalLayout buttonLayout = new HorizontalLayout(approveBtn, rejectBtn);
                buttonLayout.setSpacing(true);

                VerticalLayout content = new VerticalLayout(
                                new H2("Purchase Request Approval Console"),
                                buildHeaderSection(),
                                buildBudgetSection(),
                                new H3("Line Items Sourcing Management (Partial Approval Allowed)"),
                                lineGrid,
                                new H3("Verification Documents"),
                                documentGrid,
                                comments,
                                buttonLayout);

                content.setWidthFull();
                content.setPadding(true);
                content.setSpacing(true);

                Scroller scroller = new Scroller(content);
                scroller.setSizeFull();
                add(scroller);
        }

        @Override
        public void beforeEnter(BeforeEnterEvent event) {
                Long approvalId = Long.parseLong(event.getRouteParameters().get("id").get());

                approval = approvalsService.getAssigningApprovalById(approvalId)
                                .orElseThrow(() -> new RuntimeException("Approval task record not found"));

                header = headerService.getPurchaseRequestHeaderById(approval.getReferenceId())
                                .orElseThrow(() -> new RuntimeException("Associated Purchase Request not found"));

                bindHeaderData();
                loadDepartmentBudget();
                loadLinesData();
                loadDocuments();
                evaluateGroupAccessControlPermissions();
        }

        private VerticalLayout buildHeaderSection() {
                VerticalLayout layout = new VerticalLayout(requestId, createdBy, department, requestedTotalAmount, approvedTotalAmount, createdDate, level, status);
                layout.setSpacing(false);
                layout.setPadding(true);
                layout.getStyle().set("background", "#f5f5f5").set("border-radius", "6px");
                return layout;
        }

        private VerticalLayout buildBudgetSection() {
                VerticalLayout layout = new VerticalLayout(budgetTitle, budgetYear, totalBudgetAmount, remainingBudgetAmount);
                layout.setSpacing(false);
                layout.setPadding(true);
                layout.getStyle().set("background", "#f0fdf4").set("border-radius", "6px").set("margin-top", "10px");
                return layout;
        }

        private void bindHeaderData() {
                requestId.setText("Purchase Request ID : " + header.getPurchaseRequestId());
                createdBy.setText("Created By : " + (header.getCreatedBy() != null ? header.getCreatedBy().getEmployeeName() : "-"));
                department.setText("Department : " + (header.getForDepartment() != null ? header.getForDepartment().getDepartmentName() : "-"));
                requestedTotalAmount.setText("Total Amount Requested (Original Snapshot) : " + header.getTotalAmount());
                createdDate.setText("Created Date : " + header.getCreatedDate());
                status.setText("Task Group Status : " + approval.getStatus());
                level.setText("Active Approval Level Tier : " + approval.getLevel());
        }

        private void evaluateGroupAccessControlPermissions() {
                Employee currentEmployee = securityService.getLoggedInUser().getEmployee();
                EmployeeGroup assignedGroupRequirement = approval.getEmployeeGroup();
                EmployeeGroup currentUserGroup = currentEmployee.getRole().getEmployeeGroups().iterator().next();

                boolean isTaskPending = approval.getStatus().equals(Status.WAITING_APPROVAL);
                boolean isUserInAssignedGroup = (currentUserGroup == assignedGroupRequirement || currentUserGroup == EmployeeGroup.SUPER_ADMIN);

                if (isTaskPending && isUserInAssignedGroup) {
                        approveBtn.setVisible(true);
                        rejectBtn.setVisible(true);
                        comments.setReadOnly(false);
                        comments.setValue(approval.getComments() == null ? "" : approval.getComments());
                        
                        if (!isBudgetConfigured) {
                                approveBtn.setEnabled(false);
                                comments.setPlaceholder("CRITICAL: Approval blocked because the department budget ledger is not configured.");
                        } else {
                                approveBtn.setEnabled(true);
                        }
                } else {
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        comments.setValue(approval.getComments() == null ? "No comments provided." : approval.getComments());
                        comments.setReadOnly(true);
                }
        }

        private void loadDepartmentBudget() {
                if (header.getForDepartment() == null) {
                        isBudgetConfigured = false;
                        return;
                }

                DepartmentBudget budget = departmentBudgetService.getByDepartmentAndYear(header.getForDepartment(), Year.now());

                if (budget == null) {
                        budgetTitle.setText("Department Budget Matrix Not Configured for " + Year.now().getValue());
                        budgetYear.setText("");
                        totalBudgetAmount.setText("");
                        remainingBudgetAmount.setText("");
                        remainingBudget = 0.0;
                        isBudgetConfigured = false; 
                        return;
                }

                budgetTitle.setText("Department Budget Ledger Verification");
                budgetYear.setText("Fiscal Ledger Year : " + budget.getYear());
                totalBudgetAmount.setText("Total Allocated Budget : " + budget.getTotalBudgetAmount());
                remainingBudgetAmount.setText("Available Balance before processing : " + budget.getRemainingBudgetAmount());
                remainingBudget = budget.getRemainingBudgetAmount();
                isBudgetConfigured = true; 
        }

        private void configureLineGrid() {
                lineGrid.removeAllColumns();

                lineGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item Name").setAutoWidth(true);

                lineGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                                .setHeader("Specification").setAutoWidth(true);

                // FIXED: Direct relational mapping value lookup off the linked ItemVariant entity record configuration parameters
                lineGrid.addColumn(line -> (line.getItemVariant() != null && line.getItemVariant().getEstimatedUnitPrice() != null) 
                                ? line.getItemVariant().getEstimatedUnitPrice() : 0.0)
                                .setHeader("Est. Unit Price").setWidth("130px");

                lineGrid.addColumn(PurchaseRequestLine::getRequestedQuantity).setHeader("Requested Qty").setWidth("130px");

                lineGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                && line.getItemVariant().getItem().getUnit() != null
                                ? line.getItemVariant().getItem().getUnit().getCode() : "").setHeader("Unit").setWidth("90px");

                lineGrid.addComponentColumn(line -> {
                        NumberField approvedQtyField = new NumberField();
                        approvedQtyField.setWidth("140px");

                        if (line.getApprovedQuantity() == null) {
                                line.setApprovedQuantity(line.getRequestedQuantity());
                        }

                        approvedQtyField.setValue(line.getApprovedQuantity());
                        approvedQtyField.setMin(0.0);
                        approvedQtyField.setMax(line.getRequestedQuantity());
                        approvedQtyField.setStepButtonsVisible(true);

                        Employee currentEmployee = securityService.getLoggedInUser().getEmployee();
                        EmployeeGroup currentUserGroup = currentEmployee.getRole().getEmployeeGroups().iterator().next();
                        
                        boolean canModify = approval.getStatus().equals(Status.WAITING_APPROVAL) &&
                                        (currentUserGroup == approval.getEmployeeGroup() || currentUserGroup == EmployeeGroup.SUPER_ADMIN) &&
                                        isBudgetConfigured; 

                        approvedQtyField.setReadOnly(!canModify);

                        approvedQtyField.addValueChangeListener(e -> {
                                if (e.getValue() != null) {
                                        if (e.getValue() > line.getRequestedQuantity()) {
                                                Notification.show("Approved quantity cannot exceed original requested levels.", 3000, Position.MIDDLE);
                                                approvedQtyField.setValue(line.getRequestedQuantity());
                                                line.setApprovedQuantity(line.getRequestedQuantity());
                                        } else {
                                                line.setApprovedQuantity(e.getValue());
                                        }
                                        recalculateTotalApprovalEvaluationAmount();
                                }
                        });

                        return approvedQtyField;
                }).setHeader("Approved Qty").setWidth("160px");

                lineGrid.addColumn(PurchaseRequestLine::getItemTotalAmount).setHeader("Line Total (Est.)").setWidth("140px");

                lineGrid.addColumn(PurchaseRequestLine::getDescription).setHeader("Description").setAutoWidth(true);
                lineGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                lineGrid.setAllRowsVisible(true);
        }

        private void configureDocumentGrid() {
                documentGrid.removeAllColumns();
                documentGrid.addColumn(PurchaseRequestDocument::getFileName).setHeader("File Name").setAutoWidth(true);
                documentGrid.addColumn(PurchaseRequestDocument::getFileType).setHeader("File Type").setWidth("180px");
                documentGrid.addColumn(doc -> doc.getFileSize() != null ? (doc.getFileSize() / 1024) + " KB" : "0 KB").setHeader("Size").setWidth("120px");

                documentGrid.addComponentColumn(document -> {
                        Button previewBtn = new Button("View Document");
                        previewBtn.addThemeName("small primary");
                        previewBtn.addClickListener(e -> launchDocumentModalPreview(document));
                        return previewBtn;
                }).setHeader("Actions").setWidth("160px");

                documentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                documentGrid.setAllRowsVisible(true);
        }

        private void loadLinesData() {
                workingLinesList.clear();
                workingLinesList.addAll(lineService.getPurchaseRequestLineByHeader(header));
                lineGrid.setItems(workingLinesList);
                recalculateTotalApprovalEvaluationAmount();
        }

        private void loadDocuments() {
                documentGrid.setItems(documentService.getByPurchaseRequestHeader(header));
        }

        private void recalculateTotalApprovalEvaluationAmount() {
                double liveAdjustedTotal = 0.0;
                double liveRequestedTotal = 0.0;

                for (PurchaseRequestLine line : workingLinesList) {
                        // Get unit price straight from the ItemVariant entity model mapping
                        double price = (line.getItemVariant() != null && line.getItemVariant().getEstimatedUnitPrice() != null) 
                                        ? line.getItemVariant().getEstimatedUnitPrice() : 0.0;
                        
                        double requestedQty = line.getRequestedQuantity() != null ? line.getRequestedQuantity() : 0.0;
                        double approvedQty = line.getApprovedQuantity() != null ? line.getApprovedQuantity() : 0.0;

                        line.setItemUnitPrice(price);
                        
                        line.setItemTotalAmount(price * approvedQty);
                   
                        liveAdjustedTotal += line.getItemTotalAmount();
                        liveRequestedTotal += (price * requestedQty);
                }
                
                lineGrid.getDataProvider().refreshAll();
                
                requestedTotalAmount.setText("Total Amount Requested (Original Snapshot): " + liveRequestedTotal);
                approvedTotalAmount.setText("Total Amount Evaluated (Live Approved Quantities): " + liveAdjustedTotal);
                
                // Live visual warning indicator badge if total breaches remaining budget boundaries
                if (remainingBudget < liveAdjustedTotal) {
                        approvedTotalAmount.getStyle().set("color", "#d32f2f").set("font-weight", "bold");
                } else {
                        approvedTotalAmount.getStyle().set("color", "#2e7d32").set("font-weight", "bold");
                }
        }

        private double getFinalCalculatedTotal() {
                return workingLinesList.stream()
                                .mapToDouble(l -> l.getItemTotalAmount() != null ? l.getItemTotalAmount() : 0.0)
                                .sum();
        }

        private void approveRequest() {
                if (!isBudgetConfigured) {
                        Notification.show("Compliance Fault: Action rejected because the Department Budget ledger is missing.", 4000, Position.TOP_CENTER);
                        return;
                }

                double finalAmount = getFinalCalculatedTotal();

                if (remainingBudget < finalAmount) {
                        Notification.show("Compliance Block: Adjusted total amount exceeds available Department Budget parameters.", 4000, Position.TOP_CENTER);
                        return;
                }

                try {
                        Employee actingEmployeeGroupMember = securityService.getLoggedInUser().getEmployee();

                        for (PurchaseRequestLine approvedLine : workingLinesList) {
                                if (approvedLine.getApprovedQuantity() == 0) {
                                        approvedLine.setStatus(Status.REJECTED);
                                } else if (approvedLine.getApprovedQuantity() < approvedLine.getRequestedQuantity()) {
                                        approvedLine.setStatus(Status.PARTIALLY_APPROVED);
                                } else {
                                        approvedLine.setStatus(Status.APPROVED);
                                }
                                lineService.updatePurchaseRequestLine(approvedLine);
                        }

                        approval.setStatus(Status.APPROVED);
                        approval.setComments(comments.getValue() != null ? comments.getValue().trim() : "Approved by group.");
                        approval.setApprovedDate(LocalDate.now());
                        approval.setApprover(actingEmployeeGroupMember);

                        approvalsService.updateApprovals(approval, actingEmployeeGroupMember);

                        Notification.show("Task approved and line items updated successfully.", 3000, Position.TOP_CENTER);
                        getUI().ifPresent(ui -> ui.navigate("purchase-request"));

                } catch (Exception exception) {
                        Notification.show("Error processing approval transaction: " + exception.getMessage(), 5000, Position.MIDDLE);
                }
        }

        private void rejectRequest() {
                if (comments.isEmpty()) {
                        Notification.show("Rejection requires descriptive remarks input inside comments field.", 3000, Position.TOP_CENTER);
                        return;
                }

                try {
                        Employee actingEmployeeGroupMember = securityService.getLoggedInUser().getEmployee();

                        for (PurchaseRequestLine line : workingLinesList) {
                                line.setApprovedQuantity(0.0);
                                line.setItemTotalAmount(0.0);
                                line.setStatus(Status.REJECTED);
                                lineService.updatePurchaseRequestLine(line);
                        }

                        approval.setStatus(Status.REJECTED);
                        approval.setComments(comments.getValue().trim());
                        approval.setApprovedDate(LocalDate.now());
                        approval.setApprover(actingEmployeeGroupMember);

                        approvalsService.updateApprovals(approval, actingEmployeeGroupMember);

                        Notification.show("Purchase request verification rejected successfully.", 3000, Position.TOP_CENTER);
                        getUI().ifPresent(ui -> ui.navigate("purchase-request"));

                } catch (Exception exception) {
                        Notification.show("Error processing rejection workflow: " + exception.getMessage(), 5000, Position.MIDDLE);
                }
        }

        private void launchDocumentModalPreview(PurchaseRequestDocument document) {
                getUI().ifPresent(ui -> {
                        StreamResource resource = new StreamResource(document.getFileName(),
                                        () -> new ByteArrayInputStream(document.getDocumentData()));
                        resource.setContentType(document.getFileType());

                        var registration = ui.getSession().getResourceRegistry().registerResource(resource);
                        String url = registration.getResourceUri().toString();

                        Dialog previewDialog = new Dialog();
                        previewDialog.setWidth("80vw");
                        previewDialog.setHeight("85vh");

                        Button closeButton = new Button("Close Preview", e -> previewDialog.close());
                        closeButton.addThemeName("error small");

                        VerticalLayout dialogContent = new VerticalLayout();
                        dialogContent.setSizeFull();

                        String fileType = document.getFileType() != null ? document.getFileType().toLowerCase() : "";

                        if (fileType.startsWith("image/")) {
                                com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(url, document.getFileName());
                                image.setWidthFull();
                                image.setMaxHeight("75vh");
                                image.getStyle().set("object-fit", "contain");
                                dialogContent.add(closeButton, image);
                        } else if ("application/pdf".equals(fileType)) {
                                com.vaadin.flow.component.Html pdfViewer = new com.vaadin.flow.component.Html(
                                                "<object data='" + url + "' type='application/pdf' width='100%' height='100%' style='min-height:70vh;'></object>");
                                dialogContent.add(closeButton, pdfViewer);
                        } else {
                                Button downloadButton = new Button("Download Document Attachment");
                                downloadButton.addThemeName("success");
                                downloadButton.addClickListener(e -> ui.getPage().open(url, "_blank"));

                                dialogContent.add(closeButton, new Span("Inline display rendering engine is unavailable for this format extension."), downloadButton);
                        }

                        previewDialog.add(dialogContent);
                        previewDialog.open();
                });
        }
}