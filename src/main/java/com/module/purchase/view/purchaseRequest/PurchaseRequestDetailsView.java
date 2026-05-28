package com.module.purchase.view.purchaseRequest;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.PurchaseRequestDocumentService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
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
public class PurchaseRequestDetailsView extends VerticalLayout
                implements BeforeEnterObserver {

        private final PurchaseRequestHeaderService headerService;

        private final AssigningApprovalsService assigningApprovalsService;

        private final PurchaseRequestLineService purchaseRequestLineService;

        private final PurchaseRequestDocumentService documentService;

        private final SecurityService securityService;

        private PurchaseRequestHeader header;

        // ================= HEADER UI =================

        private final Span requestId = new Span();

        private final Span createdBy = new Span();

        private final Span department = new Span();

        private final Span totalAmount = new Span();

        private final Span createdDate = new Span();

        private final Span status = new Span();

        // ================= ACTIONS =================

        private final HorizontalLayout actionLayout = new HorizontalLayout();

        // ================= GRIDS =================

        private final Grid<PurchaseRequestLine> lineGrid = new Grid<>(PurchaseRequestLine.class, false);

        private final Grid<AssigningApprovals> approvalGrid = new Grid<>(AssigningApprovals.class, false);

        // ================= DOCUMENTS =================

        private final Grid<PurchaseRequestDocument> documentGrid = new Grid<>(PurchaseRequestDocument.class, false);

        // ================= CONSTRUCTOR =================

        public PurchaseRequestDetailsView(

                        PurchaseRequestHeaderService headerService,

                        AssigningApprovalsService assigningApprovalsService,

                        PurchaseRequestLineService purchaseRequestLineService,

                        PurchaseRequestDocumentService documentService,

                        SecurityService securityService

        ) {

                this.headerService = headerService;

                this.assigningApprovalsService = assigningApprovalsService;

                this.purchaseRequestLineService = purchaseRequestLineService;

                this.documentService = documentService;

                this.securityService = securityService;

                setSizeFull();

                setPadding(false);

                setSpacing(false);

                configureGrids();

                VerticalLayout headerSection = buildHeaderSection();

                headerSection.setWidthFull();

                documentGrid.setWidthFull();
                documentGrid.setAllRowsVisible(true);
                VerticalLayout content = new VerticalLayout(

                                new H2("Purchase Request Details"),

                                headerSection,

                                actionLayout,

                                new H3("Line Items"),

                                lineGrid,

                                new H3("Approval Flow"),

                                approvalGrid,

                                new H3("Documents"),

                                documentGrid);

                content.setWidthFull();

                content.setPadding(true);

                content.setSpacing(true);

                Scroller scroller = new Scroller(content);

                scroller.setSizeFull();

                add(scroller);
        }

        // ================= ROUTE =================

        @Override
        public void beforeEnter(BeforeEnterEvent event) {

                Long id = Long.parseLong(

                                event.getRouteParameters()
                                                .get("id")
                                                .get());

                header = headerService

                                .getPurchaseRequestHeaderById(id)

                                .orElseThrow(() ->

                                new RuntimeException(
                                                "Request not found"));

                bindHeader();

                loadGrids();

                loadDocuments();

                configureActions();
        }

        // ================= HEADER =================

        private VerticalLayout buildHeaderSection() {

                VerticalLayout layout = new VerticalLayout(

                                requestId,

                                createdBy,

                                department,

                                totalAmount,

                                createdDate,

                                status);

                layout.setPadding(true);

                layout.setSpacing(false);

                layout.setWidthFull();

                layout.getStyle()

                                .set("background", "#f9f9f9")

                                .set("border", "1px solid #ddd")

                                .set("border-radius", "8px");

                return layout;
        }

        private void bindHeader() {

                requestId.setText(
                                "Request ID : "
                                                + header.getPurchaseRequestId());

                createdBy.setText(
                                "Created By : "
                                                + (

                                                header.getCreatedBy() != null

                                                                ? header.getCreatedBy()
                                                                                .getEmployeeName()

                                                                : "-"));

                department.setText(
                                "Department : "
                                                + (

                                                header.getForDepartment() != null

                                                                ? header.getForDepartment()
                                                                                .getDepartmentName()

                                                                : "-"));

                totalAmount.setText(
                                "Total Amount : "
                                                + header.getTotalAmount());

                createdDate.setText(
                                "Created Date : "
                                                + header.getCreatedDate());

                status.setText(
                                "Status : "
                                                + header.getStatus());
        }

        // ================= ACTIONS =================

        private void configureActions() {

                actionLayout.removeAll();

                if (header.getStatus() == Status.DRAFT
                                && (

                                securityService.getLoggedInUser()
                                                .getEmployee()
                                                .getEmployeeId()
                                                .equals(
                                                                header.getCreatedBy()
                                                                                .getEmployeeId())

                                                ||

                                                securityService.getLoggedInUser()
                                                                .getEmployee()
                                                                .getRole()
                                                                .getEmployeeGroups()
                                                                .contains(EmployeeGroup.SUPER_ADMIN)

                                                ||

                                                securityService.getLoggedInUser()
                                                                .getEmployee()
                                                                .getRole()
                                                                .getEmployeeGroups()
                                                                .contains(EmployeeGroup.MANAGER)

                                )) {

                        Button editButton = new Button("Edit Request");

                        editButton.addClickListener(e -> {

                                getUI().ifPresent(ui ->

                                ui.navigate( "purchase-request-form/"+ header.getPurchaseRequestId()));
                        });

                        Button deleteButton = new Button("Delete Request");

                        deleteButton.addClickListener(e -> {

                                headerService.deletePurchaseRequestHeaderById(

                                                header.getPurchaseRequestId(),

                                                securityService.getLoggedInUser()
                                                                .getEmployee());

                                Notification.show(
                                                "Purchase Request Deleted");

                                getUI().ifPresent(ui ->

                                ui.navigate("purchase-request"));
                        });

                        actionLayout.add(
                                        editButton,
                                        deleteButton);
                }

                // ================= WAITING APPROVAL =================

                if (header.getStatus() == Status.WAITING_APPROVAL && (securityService.getLoggedInUser()
                                                .getEmployee()
                                                .getEmployeeId()
                                                .equals(header.getCreatedBy().getEmployeeId())

                                                ||securityService.getLoggedInUser().getEmployee()
                                                                .getRole()
                                                                .getEmployeeGroups()
                                                                .contains(EmployeeGroup.SUPER_ADMIN)

                                                ||  securityService.getLoggedInUser()
                                                                .getEmployee().getRole().getEmployeeGroups()
                                                                .contains(EmployeeGroup.MANAGER)

                                )) {

                        Button cancelButton = new Button("Cancel Request");

                        cancelButton.addClickListener(e -> {

                                header.setStatus(
                                                Status.CANCELLED);

                                headerService.updatePurchaseRequestHeader(

                                                header,

                                                securityService.getLoggedInUser()
                                                                .getEmployee());

                                Notification.show(
                                                "Purchase Request Cancelled");

                                bindHeader();

                                configureActions();
                        });

                        actionLayout.add(cancelButton);
                }
        }

        // ================= GRID CONFIG =================

        private void configureGrids() {

                // ================= LINE GRID =================

                lineGrid.addColumn(
                                PurchaseRequestLine::getPurchaseRequestLineId)
                                .setHeader("Line ID");

                lineGrid.addColumn(line ->

                line.getItem() != null

                                ? line.getItem()
                                                .getItemName()

                                : "")

                                .setHeader("Item Name");

                lineGrid.addColumn(
                                PurchaseRequestLine::getQuantity)
                                .setHeader("Quantity");

                lineGrid.addColumn(
                                PurchaseRequestLine::getUnitPrice)
                                .setHeader("Unit Price");

                lineGrid.addColumn(line ->

                line.getItem() == null

                                ? " "

                                : line.getItem().getVATCode()

                ).setHeader("VAT Code");

                lineGrid.addColumn(
                                PurchaseRequestLine::getDiscount)
                                .setHeader("Discount");

                lineGrid.addColumn(
                                PurchaseRequestLine::getTotalPrice)
                                .setHeader("Total Price");

                lineGrid.setWidthFull();

                lineGrid.setAllRowsVisible(true);

                // ================= APPROVAL GRID =================

                approvalGrid.addColumn(
                                AssigningApprovals::getLevel)
                                .setHeader("Level");

                approvalGrid.addColumn(a ->

                a.getApprover() != null

                                ? a.getApprover()
                                                .getEmployeeName()

                                : "")

                                .setHeader("Approver");

                approvalGrid.addColumn(
                                AssigningApprovals::getAssignedDate)
                                .setHeader("Assigned Date");

                approvalGrid.addColumn(a ->

                a.getStatus() != null

                                ? a.getStatus().name()

                                : "")

                                .setHeader("Status");

                approvalGrid.setWidthFull();

                approvalGrid.setAllRowsVisible(true);

                documentGrid.addColumn(
                                PurchaseRequestDocument::getFileName)
                                .setHeader("File Name");

                documentGrid.addColumn(
                                PurchaseRequestDocument::getFileType)
                                .setHeader("Type");

                documentGrid.addColumn(
                                PurchaseRequestDocument::getFileSize)
                                .setHeader("Size");

                documentGrid.setWidthFull();

                documentGrid.setAllRowsVisible(true);

                documentGrid.addItemDoubleClickListener(event -> {
                        PurchaseRequestDocument document = event.getItem();
                        StreamResource resource = new StreamResource(
                                        document.getFileName(),
                                        () -> new ByteArrayInputStream(document.getDocumentData()));
                        getUI().ifPresent(ui -> {
                                var registration = ui.getSession()
                                                .getResourceRegistry()
                                                .registerResource(resource);

                                String url = registration.getResourceUri()
                                                .toString();

                                ui.getPage().open(url, "_blank");
                        });
                });

        }

        // ================= LOAD DATA =================

        private void loadGrids() {

                lineGrid.setItems(

                                purchaseRequestLineService
                                                .getPurchaseRequestLineByHeader(
                                                                header));

                approvalGrid.setItems(assigningApprovalsService
                                .getAssigningApprovalByTypeAndReferId(ApprovalType.PURCHASE_REQUEST_APPROVAL,
                                                header.getPurchaseRequestId()));
        }

        // ================= DOCUMENTS =================

        private void loadDocuments() {

                List<PurchaseRequestDocument> documents = documentService.getByPurchaseRequestHeader(header);

                documentGrid.setItems(documents);
        }
}