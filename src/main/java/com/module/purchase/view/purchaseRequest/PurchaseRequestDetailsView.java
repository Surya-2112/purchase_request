package com.module.purchase.view.purchaseRequest;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Needs;
import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.NeedsService;
import com.module.purchase.service.PurchaseRequestDocumentService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.RepeatedPeriodService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-details/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestDetailsView extends VerticalLayout implements BeforeEnterObserver {

        private final PurchaseRequestHeaderService headerService;
        private final AssigningApprovalsService assigningApprovalsService;
        private final PurchaseRequestLineService purchaseRequestLineService;
        private final PurchaseRequestDocumentService documentService;
        private final SecurityService securityService;
        private final RepeatedPeriodService repeatedPeriodService;
        private final NeedsService needService;

        private PurchaseRequestHeader header;

        private final Span requestId = new Span();
        private final Span createdBy = new Span();
        private final Span department = new Span();
        private final Span createdDate = new Span();
        private final Span status = new Span();

        private final HorizontalLayout actionLayout = new HorizontalLayout();
        private final Grid<PurchaseRequestLine> lineGrid = new Grid<>(PurchaseRequestLine.class, false);
        private final Grid<AssigningApprovals> approvalGrid = new Grid<>(AssigningApprovals.class, false);
        private final Grid<PurchaseRequestDocument> documentGrid = new Grid<>(PurchaseRequestDocument.class, false);

        private final VerticalLayout repeatedPeriodsSection = new VerticalLayout();
        private final Grid<PurchaseRequestLine> repeatedPeriodsGrid = new Grid<>(PurchaseRequestLine.class, false);

        private final VerticalLayout previewOverlay = new VerticalLayout();
        private final Dialog previewDialog = new Dialog();

        public PurchaseRequestDetailsView(PurchaseRequestHeaderService headerService,
                        AssigningApprovalsService assigningApprovalsService,
                        PurchaseRequestLineService purchaseRequestLineService,
                        PurchaseRequestDocumentService documentService, 
                        SecurityService securityService,
                        NeedsService needService,
                        RepeatedPeriodService repeatedPeriodService) {

                this.headerService = headerService;
                this.assigningApprovalsService = assigningApprovalsService;
                this.purchaseRequestLineService = purchaseRequestLineService;
                this.documentService = documentService;
                this.securityService = securityService;
                this.needService= needService;
                this.repeatedPeriodService = repeatedPeriodService;

                setSizeFull();
                setPadding(false);
                setSpacing(false);

                configureGrids();
                configureRepeatedPeriodsGrid(); 

                VerticalLayout headerSection = buildHeaderSection();
                headerSection.setWidthFull();

                repeatedPeriodsSection.add(new H3("Associated Active Recurring Task Schedules"), repeatedPeriodsGrid);
                repeatedPeriodsSection.setPadding(false);
                repeatedPeriodsSection.setSpacing(true);
                repeatedPeriodsSection.setVisible(false); 

                VerticalLayout content = new VerticalLayout(
                                new H2("Purchase Request Details"),
                                headerSection,
                                actionLayout,
                                new H3("Line Items"),
                                lineGrid,
                                repeatedPeriodsSection, 
                                new H3("Approval Flow"),
                                approvalGrid,
                                new H3("Documents"),
                                documentGrid
                );

                content.setWidthFull();
                content.setPadding(true);
                content.setSpacing(true);

                Scroller scroller = new Scroller(content);
                scroller.setSizeFull();

                previewDialog.setWidth("80%");
                previewDialog.setHeight("90%");
                add(previewDialog);

                add(scroller, previewOverlay);
        }

        @Override
        public void beforeEnter(BeforeEnterEvent event) {
                Long id = Long.parseLong(event.getRouteParameters().get("id").get());

                header = headerService.getPurchaseRequestHeaderById(id)
                                .orElseThrow(() -> new RuntimeException("Request not found"));

                bindHeader();
                loadGrids();
                loadDocuments();
                configureActions();
        }

        private VerticalLayout buildHeaderSection() {
                VerticalLayout layout = new VerticalLayout(
                                requestId,
                                createdBy,
                                department,
                                createdDate,
                                status
                );

                layout.setPadding(true);
                layout.setSpacing(false);
                layout.setWidthFull();

                layout.getStyle().set("background", "#f9f9f9")
                                .set("border", "1px solid #ddd")
                                .set("border-radius", "8px");

                return layout;
        }

        private void bindHeader() {
                requestId.setText("Request ID : " + header.getPurchaseRequestId());

                createdBy.setText("Created By : "
                                + (header.getCreatedBy() != null ? header.getCreatedBy().getEmployeeName() : "-"));

                department.setText("Department : "
                                + (header.getForDepartment() != null ? header.getForDepartment().getDepartmentName() : "-"));

                createdDate.setText("Created Date : " + header.getCreatedDate());
                status.setText("Status : " + (header.getStatus() != null ? header.getStatus().getDisplayName() : "-"));
        }

        private void configureActions() {
                actionLayout.removeAll();

                EmployeeGroup currentUserGroup = securityService.getLoggedInUser().getEmployee().getRole().getEmployeeGroups().iterator().next();

                if (header.getStatus() == Status.DRAFT
                                && (securityService.getLoggedInUser().getEmployee().getEmployeeId().equals(header.getCreatedBy().getEmployeeId())
                                || currentUserGroup == EmployeeGroup.SUPER_ADMIN
                                || currentUserGroup == EmployeeGroup.MANAGER)) {

                        Button editButton = new Button("Edit Request");
                        editButton.addClickListener(e -> getUI().ifPresent(ui -> 
                                ui.navigate("purchase-request-form/" + header.getPurchaseRequestId())));

                        Button deleteButton = new Button("Delete Request");
                        deleteButton.addClickListener(e -> {
                                headerService.deletePurchaseRequestHeaderById(
                                                header.getPurchaseRequestId(),
                                                securityService.getLoggedInUser().getEmployee());

                                Notification.show("Purchase Request Deleted");
                                getUI().ifPresent(ui -> ui.navigate("purchase-request"));
                        });

                        actionLayout.add(editButton, deleteButton);
                }

                if (header.getStatus() == Status.WAITING_APPROVAL 
                                && (securityService.getLoggedInUser().getEmployee().getEmployeeId().equals(header.getCreatedBy().getEmployeeId())
                                || currentUserGroup == EmployeeGroup.SUPER_ADMIN
                                || currentUserGroup == EmployeeGroup.MANAGER)) {

                        Button cancelButton = new Button("Cancel Request");
                        cancelButton.addClickListener(e -> {
                                header.setStatus(Status.CANCELLED);
                                headerService.updatePurchaseRequestHeader(header, securityService.getLoggedInUser().getEmployee());
                                Notification.show("Purchase Request Cancelled");
                                bindHeader();
                                configureActions();
                        });

                        actionLayout.add(cancelButton);
                }
        }

        private void configureGrids() {
                lineGrid.removeAllColumns();
                
                lineGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                ? line.getItemVariant().getItem().getItemName() : "")
                                .setHeader("Item").setAutoWidth(true);

                lineGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                                .setHeader("Specification").setAutoWidth(true);

                lineGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                                && line.getItemVariant().getItem().getUnit() != null 
                                ? line.getItemVariant().getItem().getUnit().getCode() : "")
                                .setHeader("Unit").setWidth("100px");

                lineGrid.addColumn(PurchaseRequestLine::getRequestedQuantity).setHeader("Requested Qty").setWidth("120px");
                
                lineGrid.addColumn(line -> line.getApprovedQuantity() != null ? line.getApprovedQuantity() : "-")
                                .setHeader("Approved Qty").setWidth("130px");

                lineGrid.addColumn(PurchaseRequestLine::getDescription).setHeader("Description").setAutoWidth(true);

                lineGrid.setWidthFull();
                lineGrid.setAllRowsVisible(true);
                lineGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

                approvalGrid.removeAllColumns();
                approvalGrid.addColumn(AssigningApprovals::getLevel).setHeader("Level").setWidth("80px");
                
                approvalGrid.addColumn(a -> a.getEmployeeGroup() != null 
                                ? a.getEmployeeGroup().getDisplayName() : "")
                                .setHeader("Approver Group").setAutoWidth(true);

                approvalGrid.addColumn(AssigningApprovals::getAssignedDate).setHeader("Assigned Date").setWidth("160px");
                
                approvalGrid.addColumn(a -> a.getStatus() != null ? a.getStatus().getDisplayName() : "")
                                .setHeader("Status").setWidth("150px");

                approvalGrid.setWidthFull();
                approvalGrid.setAllRowsVisible(true);
                approvalGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

                // Document Grid Configuration
                documentGrid.removeAllColumns();
                documentGrid.addColumn(PurchaseRequestDocument::getFileName).setHeader("File Name").setAutoWidth(true);
                documentGrid.addColumn(PurchaseRequestDocument::getFileType).setHeader("Type").setWidth("150px");
                documentGrid.addColumn(doc -> doc.getFileSize() != null ? (doc.getFileSize() / 1024) + " KB" : "0 KB").setHeader("Size").setWidth("120px");

                documentGrid.addComponentColumn(document -> {
                        Button viewButton = new Button("View / Download");
                        viewButton.addThemeName("small primary");
                        
                        viewButton.addClickListener(clickEvent -> {
                                getUI().ifPresent(ui -> {
                                        StreamResource resource = new StreamResource(
                                                        document.getFileName(),
                                                        () -> new ByteArrayInputStream(document.getDocumentData()));

                                        resource.setContentType(document.getFileType());
                                        
                                        var registration = ui.getSession().getResourceRegistry().registerResource(resource);
                                        String url = registration.getResourceUri().toString();

                                        previewDialog.setWidth("75vw");
                                        previewDialog.setHeight("85vh");

                                        Button closeButton = new Button("Close Window", e -> previewDialog.close());
                                        closeButton.addThemeName("error");

                                        VerticalLayout dialogContent = new VerticalLayout();
                                        dialogContent.setSizeFull();
                                        dialogContent.setPadding(true);
                                        dialogContent.setSpacing(true);

                                        String fileType = document.getFileType() != null ? document.getFileType().toLowerCase() : "";

                                        if (fileType.startsWith("image/")) {
                                                com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(url, document.getFileName());
                                                image.getStyle().set("max-width", "100%").set("max-height", "70vh").set("object-fit", "contain");
                                                dialogContent.add(closeButton, image);
                                        } else if ("application/pdf".equals(fileType)) {
                                                com.vaadin.flow.component.Html pdfViewer = new com.vaadin.flow.component.Html(
                                                                "<object data='" + url + "' type='application/pdf' width='100%' height='100%' style='min-height:70vh;'></object>");
                                                dialogContent.add(closeButton, pdfViewer);
                                        } else {
                                                Button downloadLinkBtn = new Button("Download Document");
                                                downloadLinkBtn.addThemeName("success");
                                                downloadLinkBtn.addClickListener(e -> ui.getPage().open(url, "_blank"));

                                                dialogContent.add(
                                                                closeButton,
                                                                new Span("Inline preview is unavailable for this specific extension file type (" + fileType + ")."),
                                                                downloadLinkBtn);
                                        }

                                        previewDialog.removeAll();
                                        previewDialog.add(dialogContent);
                                        previewDialog.open();
                                });
                        });
                        return viewButton;
                }).setHeader("Action").setWidth("180px");

                documentGrid.setWidthFull();
                documentGrid.setAllRowsVisible(true);
                documentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        }

        private void configureRepeatedPeriodsGrid() {
                repeatedPeriodsGrid.removeAllColumns();

                repeatedPeriodsGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                ? line.getItemVariant().getItem().getItemName() : "")
                                .setHeader("Scheduled Item").setAutoWidth(true);

                repeatedPeriodsGrid.addColumn(line -> {
                        if (line.getRepeatableId() == null) return "-";
                        Optional<RepeatedPeriod> periodOpt = repeatedPeriodService.getRepeatedPeriodById(line.getRepeatableId());
                        if (periodOpt.isPresent()) {
                                RepeatedPeriod p = periodOpt.get();
                                return "Every " + p.getFrequencyPeriod() + " " + (p.getFrequencyType() != null ? p.getFrequencyType().name() : "");
                        }
                        return "Configuration Missing";
                }).setHeader("Recurrence Pattern").setAutoWidth(true);

                repeatedPeriodsGrid.addColumn(line -> {
                        if (line.getRepeatableId() == null) return "-";
                        return repeatedPeriodService.getRepeatedPeriodById(line.getRepeatableId())
                                        .map(p -> p.getFromDate() != null ? p.getFromDate().toString() : "-").orElse("-");
                }).setHeader("Start Date").setWidth("140px");

                repeatedPeriodsGrid.addColumn(line -> {
                        if (line.getRepeatableId() == null) return "-";
                        return repeatedPeriodService.getRepeatedPeriodById(line.getRepeatableId())
                                        .map(p -> p.getToDate() != null ? p.getToDate().toString() : "Indefinite").orElse("Indefinite");
                }).setHeader("End Date").setWidth("140px");

                repeatedPeriodsGrid.addColumn(line -> {
                        if (line.getRepeatableId() == null) return "-";
                        return repeatedPeriodService.getRepeatedPeriodById(line.getRepeatableId())
                                        .map(p -> p.getNextDate() != null ? p.getNextDate().toString() : "Processing").orElse("-");
                }).setHeader("Next Scheduled Run").setWidth("160px");

                repeatedPeriodsGrid.setWidthFull();
                repeatedPeriodsGrid.setAllRowsVisible(true);
                repeatedPeriodsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        }

        private void loadGrids() {
                List<PurchaseRequestLine> prLines = purchaseRequestLineService.getPurchaseRequestLineByHeader(header);
                 List<Needs> requestNeeds= needService.getSpecificNeedRecord(EntityType.ITEM,header.getPurchaseRequestId());
                 for(Needs line:requestNeeds)
                 {   
                  PurchaseRequestLine prLine = new PurchaseRequestLine();
                   prLine.setDescription(line.getNeedLine());
                    prLines.add(prLine);
                 }

                lineGrid.setItems(prLines);


                List<PurchaseRequestLine> repeatableLines = new ArrayList<>();
                for (PurchaseRequestLine line : prLines) {
                        if (line.getRepeatableId() != null) {
                                repeatableLines.add(line);
                        }
                }

                if (!repeatableLines.isEmpty()) {
                        repeatedPeriodsGrid.setItems(repeatableLines);
                        repeatedPeriodsSection.setVisible(true);
                } else {
                        repeatedPeriodsSection.setVisible(false);
                }


                approvalGrid.setItems(assigningApprovalsService.getAssigningApprovalByTypeAndReferId(
                                ApprovalType.PURCHASE_REQUEST,
                                header.getPurchaseRequestId()));
        }

        private void loadDocuments() {
                List<PurchaseRequestDocument> documents = documentService.getByPurchaseRequestHeader(header);
                documentGrid.setItems(documents);
        }
}