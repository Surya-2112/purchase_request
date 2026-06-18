package com.module.purchase.view.purchaseRequest;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.PurchaseRequestDocumentService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.RepeatedPeriodService;
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

@Route(value = "assigned-approvals-details/:id", layout = MainLayout.class)
@PermitAll
public class AssignedApprovalsDetailsView extends VerticalLayout implements BeforeEnterObserver {

        private final PurchaseRequestHeaderService headerService;
        private final PurchaseRequestLineService lineService;
        private final AssigningApprovalsService approvalsService;
        private final DepartmentBudgetService departmentBudgetService;
        private final PurchaseRequestDocumentService documentService;
        private final SecurityService securityService;
        private final RepeatedPeriodService repeatedPeriodService;

        private PurchaseRequestHeader header;
        private AssigningApprovals approval;

        private final Span requestId = new Span();
        private final Span createdBy = new Span();
        private final Span department = new Span();
        private final Span requestedTotalAmount = new Span();
        private final Span approvedTotalAmount = new Span();
        private final Span createdDate = new Span();
        private final Span level = new Span();
        private final Span status = new Span();

        private final H3 budgetTitle = new H3();
        private final Span budgetYear = new Span();
        private final Span totalBudgetAmount = new Span();
        private final Span remainingBudgetAmount = new Span();
        private Double remainingBudget = 0.0;
        private boolean isBudgetConfigured = false;

        private final Button approveBtn = new Button("Approve");
        private final Button rejectBtn = new Button("Reject");
        private final TextArea comments = new TextArea("Approver Comments / Remarks");

        private final Grid<PurchaseRequestLine> lineGrid = new Grid<>(PurchaseRequestLine.class, false);
        private final Grid<PurchaseRequestDocument> documentGrid = new Grid<>(PurchaseRequestDocument.class, false);

        private final VerticalLayout recurringScheduleSection = new VerticalLayout();
        private final Grid<PurchaseRequestLine> scheduleGrid = new Grid<>(PurchaseRequestLine.class, false);
        private final Map<PurchaseRequestLine, RepeatedPeriod> workingSchedulesMap = new HashMap<>();

        private final List<PurchaseRequestLine> hasRepeatedPeriod = new ArrayList<>();
        private final List<PurchaseRequestLine> workingLinesList = new ArrayList<>();
        private boolean canUserModifyData = false;

        public AssignedApprovalsDetailsView(
                        PurchaseRequestHeaderService headerService,
                        PurchaseRequestLineService lineService,
                        AssigningApprovalsService approvalsService,
                        DepartmentBudgetService departmentBudgetService,
                        PurchaseRequestDocumentService documentService,
                        SecurityService securityService,
                        RepeatedPeriodService repeatedPeriodService) {

                this.headerService = headerService;
                this.lineService = lineService;
                this.approvalsService = approvalsService;
                this.departmentBudgetService = departmentBudgetService;
                this.documentService = documentService;
                this.securityService = securityService;
                this.repeatedPeriodService = repeatedPeriodService;

                setSizeFull();
                setPadding(false);
                setSpacing(false);

                comments.setWidthFull();
                comments.setMinHeight("100px");
                comments.setPlaceholder("Enter reasons for partial approval modifications or rejections here...");

                configureLineGrid();
                configureScheduleGrid();
                configureDocumentGrid();

                approveBtn.addThemeName("primary success");
                rejectBtn.addThemeName("primary error");

                approveBtn.addClickListener(e -> approveRequest());
                rejectBtn.addClickListener(e -> rejectRequest());

                HorizontalLayout buttonLayout = new HorizontalLayout(approveBtn, rejectBtn);
                buttonLayout.setSpacing(true);

                recurringScheduleSection.add(new H3("Repeated Periods"), scheduleGrid);
                recurringScheduleSection.setPadding(false);
                recurringScheduleSection.setSpacing(true);
                recurringScheduleSection.setVisible(false);

                VerticalLayout content = new VerticalLayout(
                                new H2("Purchase Request Approval"),
                                buildHeaderSection(),
                                buildBudgetSection(),
                                new H3("Line Items"),
                                lineGrid,
                                recurringScheduleSection,
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

                try {
                        approval = approvalsService.getAssigningApprovalById(approvalId).get();

                        header = headerService.getPurchaseRequestHeaderById(approval.getReferenceId()).get();
                } catch (Exception ex) {
                        event.forwardTo("");
                        event.getUI().access(() -> {
                                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                        });
                        return;
                }
                if(!securityService.getLoggedInUser().getEmployee().getRole().getEmployeeGroups().contains(approval.getEmployeeGroup()))
                {event.forwardTo("purchase-request");
                        event.getUI().access(() -> {
                                Notification.show("Access Denied", 3000, Notification.Position.MIDDLE);
                        });
                        return;
                }

                evaluateGroupAccessControlPermissions();
                bindHeaderData();
                loadDepartmentBudget();
                loadLinesData();
                loadDocuments();
        }

        private VerticalLayout buildHeaderSection() {
                VerticalLayout layout = new VerticalLayout(requestId, createdBy, department, requestedTotalAmount,
                                approvedTotalAmount, createdDate, level, status);
                layout.setSpacing(false);
                layout.setPadding(true);
                layout.getStyle().set("background", "#f5f5f5").set("border-radius", "6px");
                return layout;
        }

        private VerticalLayout buildBudgetSection() {
                VerticalLayout layout = new VerticalLayout(budgetTitle, budgetYear, totalBudgetAmount,
                                remainingBudgetAmount);
                layout.setSpacing(false);
                layout.setPadding(true);
                layout.getStyle().set("background", "#f0fdf4").set("border-radius", "6px").set("margin-top", "10px");
                return layout;
        }

        private void bindHeaderData() {
                requestId.setText("Purchase Request ID : " + header.getPurchaseRequestId());
                createdBy.setText("Created By : "
                                + (header.getCreatedBy() != null ? header.getCreatedBy().getEmployeeName() : "-"));
                department.setText("Department : "
                                + (header.getForDepartment() != null ? header.getForDepartment().getDepartmentName()
                                                : "-"));
                requestedTotalAmount.setText("Total Amount Requested : " + header.getTotalAmount());
                createdDate.setText("Created Date : " + header.getCreatedDate());
                status.setText("Task Group Status : " + approval.getStatus());
                level.setText("Approval Level : " + approval.getLevel());
        }

        private void evaluateGroupAccessControlPermissions() {
                Employee currentEmployee = securityService.getLoggedInUser().getEmployee();
                EmployeeGroup assignedGroupRequirement = approval.getEmployeeGroup();
                List<EmployeeGroup> currentUserGroup = currentEmployee.getRole().getEmployeeGroups();

                boolean isTaskPending = approval.getStatus().equals(Status.WAITING_APPROVAL);
                boolean isUserInAssignedGroup = currentUserGroup.contains(assignedGroupRequirement)
                                || currentUserGroup.contains(EmployeeGroup.SUPER_ADMIN);

                this.canUserModifyData = isTaskPending && isUserInAssignedGroup;

                if (canUserModifyData) {
                        approveBtn.setVisible(true);
                        rejectBtn.setVisible(true);
                        comments.setReadOnly(false);
                        comments.setValue(approval.getComments() == null ? "" : approval.getComments());

                        if (!isBudgetConfigured) {
                                approveBtn.setEnabled(false);
                                comments.setPlaceholder(
                                                "CRITICAL: Approval blocked because the department budget ledger is not configured.");
                        } else {
                                approveBtn.setEnabled(true);
                        }
                } else {
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        comments.setValue(approval.getComments() == null ? "No comments provided."
                                        : approval.getComments());
                        comments.setReadOnly(true);
                }
        }

        private void loadDepartmentBudget() {
                if (header.getForDepartment() == null) {
                        isBudgetConfigured = false;
                        return;
                }

                DepartmentBudget budget = departmentBudgetService.getByDepartmentAndYear(header.getForDepartment(),
                                Year.now());

                if (budget == null) {
                        budgetTitle.setText("Department Budget Not Configured for " + Year.now().getValue());
                        budgetYear.setText("");
                        totalBudgetAmount.setText("");
                        remainingBudgetAmount.setText("");
                        remainingBudget = 0.0;
                        isBudgetConfigured = false;
                        return;
                }

                budgetTitle.setText("Department Budget");
                budgetYear.setText("Year : " + budget.getYear());
                totalBudgetAmount.setText("Total Allocated Budget : " + budget.getTotalBudgetAmount());
                remainingBudgetAmount.setText("Available Balance : " + budget.getRemainingBudgetAmount());
                remainingBudget = budget.getRemainingBudgetAmount();
                isBudgetConfigured = true;

                evaluateGroupAccessControlPermissions();
        }

        private void configureLineGrid() {
                lineGrid.removeAllColumns();

                lineGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                ? line.getItemVariant().getItem().getItemName()
                                : "").setHeader("Item Name").setAutoWidth(true);

                lineGrid.addColumn(
                                line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                                .setHeader("Specification").setAutoWidth(true);

                lineGrid.addColumn(line -> (line.getItemVariant() != null
                                && line.getItemVariant().getEstimatedUnitPrice() != null)
                                                ? line.getItemVariant().getEstimatedUnitPrice()
                                                : 0.0)
                                .setHeader("Unit Price").setWidth("130px");

                lineGrid.addColumn(PurchaseRequestLine::getRequestedQuantity).setHeader("Requested Qty")
                                .setWidth("130px");

                lineGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                && line.getItemVariant().getItem().getUnit() != null
                                                ? line.getItemVariant().getItem().getUnit().getCode()
                                                : "")
                                .setHeader("Unit").setWidth("90px");

                lineGrid.addComponentColumn(line -> {
                        NumberField approvedQtyField = new NumberField();
                        approvedQtyField.setWidth("120px");

                        if (line.getApprovedQuantity() == null) {
                                line.setApprovedQuantity(line.getRequestedQuantity());
                        }

                        approvedQtyField.setValue(line.getApprovedQuantity());
                        approvedQtyField.setMin(0.0);
                        approvedQtyField.setMax(line.getRequestedQuantity());
                        approvedQtyField.setStepButtonsVisible(true);
                        approvedQtyField.setReadOnly(!canUserModifyData || !isBudgetConfigured);

                        approvedQtyField.addValueChangeListener(e -> {
                                if (e.getValue() != null) {
                                        if (e.getValue() > line.getRequestedQuantity()) {
                                                Notification.show(
                                                                "Approved quantity cannot exceed original requested levels.",
                                                                3000, Position.MIDDLE);
                                                approvedQtyField.setValue(line.getRequestedQuantity());
                                                line.setApprovedQuantity(line.getRequestedQuantity());
                                        } else {
                                                line.setApprovedQuantity(e.getValue());
                                        }
                                        recalculateTotalApprovalEvaluationAmount();
                                        scheduleGrid.getDataProvider().refreshAll();
                                }
                        });

                        return approvedQtyField;
                }).setHeader("Approved Qty").setWidth("160px");

                lineGrid.addColumn(PurchaseRequestLine::getItemTotalAmount).setHeader("Line Total").setWidth("140px");
                lineGrid.addColumn(PurchaseRequestLine::getDescription).setHeader("Description").setAutoWidth(true);
                lineGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                lineGrid.setAllRowsVisible(true);
        }

        private void configureScheduleGrid() {
                scheduleGrid.removeAllColumns();

                scheduleGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                                ? line.getItemVariant().getItem().getItemName()
                                : "").setHeader("Scheduled Item").setAutoWidth(true);

                scheduleGrid.addColumn(line -> {
                        RepeatedPeriod p = workingSchedulesMap.get(line);
                        if (p == null)
                                return "No Schedule Active";
                        return "Every " + p.getFrequencyPeriod() + " "
                                        + (p.getFrequencyType() != null ? p.getFrequencyType().name() : "");
                }).setHeader("Recurrence Interval").setAutoWidth(true);

                scheduleGrid.addColumn(line -> {
                        RepeatedPeriod p = workingSchedulesMap.get(line);
                        return (p != null && p.getFromDate() != null) ? p.getFromDate().toString() : "-";
                }).setHeader("Start Date").setWidth("140px");

                scheduleGrid.addColumn(line -> {
                        RepeatedPeriod p = workingSchedulesMap.get(line);
                        return (p != null && p.getToDate() != null) ? p.getToDate().toString() : "Indefinite";
                }).setHeader("End Date").setWidth("140px");

                scheduleGrid.addComponentColumn(line -> {
                        Button modifyScheduleBtn = new Button("Modify Schedule", e -> {
                                RepeatedPeriod currentPeriod = workingSchedulesMap.get(line);
                                AutoRfqScheduleDialog dialog = new AutoRfqScheduleDialog(updatedPeriod -> {
                                        currentPeriod.setFrequencyPeriod(updatedPeriod.getFrequencyPeriod());
                                        currentPeriod.setFrequencyType(updatedPeriod.getFrequencyType());
                                        currentPeriod.setFromDate(updatedPeriod.getFromDate());
                                        currentPeriod.setToDate(updatedPeriod.getToDate());
                                        currentPeriod.setNextDate(updatedPeriod.getNextDate());
                                        workingSchedulesMap.put(line, currentPeriod);
                                        scheduleGrid.getDataProvider().refreshAll();
                                        Notification.show("Repeated preriod updated successfully.");
                                });
                                dialog.open();
                        });
                        modifyScheduleBtn.addThemeName("small primary");
                        modifyScheduleBtn.setEnabled(canUserModifyData && isBudgetConfigured);

                        Button clearScheduleBtn = new Button("Remove", e -> {

                                workingSchedulesMap.remove(line);
                                scheduleGrid.getDataProvider().refreshAll();
                                recurringScheduleSection.setVisible(!workingSchedulesMap.isEmpty());
                                Notification.show("Repeated preriod removed successfully.");
                        });
                        clearScheduleBtn.addThemeName("small error");
                        clearScheduleBtn.setEnabled(canUserModifyData && isBudgetConfigured);

                        return new HorizontalLayout(modifyScheduleBtn, clearScheduleBtn);
                }).setHeader("Scheduling Maintenance Actions").setWidth("320px");

                scheduleGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                scheduleGrid.setAllRowsVisible(true);
        }

        private void configureDocumentGrid() {
                documentGrid.removeAllColumns();
                documentGrid.addColumn(PurchaseRequestDocument::getFileName).setHeader("File Name").setAutoWidth(true);
                documentGrid.addColumn(PurchaseRequestDocument::getFileType).setHeader("File Type").setWidth("180px");
                documentGrid.addColumn(doc -> doc.getFileSize() != null ? (doc.getFileSize() / 1024) + " KB" : "0 KB")
                                .setHeader("Size").setWidth("120px");

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
                workingSchedulesMap.clear();

                List<PurchaseRequestLine> dbLines = lineService.getPurchaseRequestLineByHeader(header);
                workingLinesList.addAll(dbLines);
                lineGrid.setItems(workingLinesList);

                List<PurchaseRequestLine> repeatableLines = new ArrayList<>();
                for (PurchaseRequestLine line : dbLines) {

                        if (line.getApprovedQuantity() == null) {
                                line.setApprovedQuantity(line.getRequestedQuantity());
                        }

                        if (line.getRepeatableId() != null) {
                                Optional<RepeatedPeriod> periodOpt = repeatedPeriodService
                                                .getRepeatedPeriodById(line.getRepeatableId());
                                if (periodOpt.isPresent()) {
                                        workingSchedulesMap.put(line, periodOpt.get());
                                        hasRepeatedPeriod.add(line);
                                        repeatableLines.add(line);
                                }
                        }
                }

                if (!repeatableLines.isEmpty()) {
                        scheduleGrid.setItems(repeatableLines);
                        recurringScheduleSection.setVisible(true);
                } else {
                        recurringScheduleSection.setVisible(false);
                }

                recalculateTotalApprovalEvaluationAmount();
        }

        private void loadDocuments() {
                documentGrid.setItems(documentService.getByPurchaseRequestHeader(header));
        }

        private void recalculateTotalApprovalEvaluationAmount() {
                double liveAdjustedTotal = 0.0;
                double liveRequestedTotal = 0.0;

                for (PurchaseRequestLine line : workingLinesList) {
                        double price = (line.getItemVariant() != null
                                        && line.getItemVariant().getEstimatedUnitPrice() != null)
                                                        ? line.getItemVariant().getEstimatedUnitPrice()
                                                        : 0.0;

                        double requestedQty = line.getRequestedQuantity() != null ? line.getRequestedQuantity() : 0.0;

                        if (line.getApprovedQuantity() == null) {
                                line.setApprovedQuantity(requestedQty);
                        }
                        double approvedQty = line.getApprovedQuantity();

                        line.setItemUnitPrice(price);
                        line.setItemTotalAmount(price * approvedQty);

                        liveAdjustedTotal += line.getItemTotalAmount();
                        liveRequestedTotal += (price * requestedQty);
                }

                lineGrid.getDataProvider().refreshAll();

                requestedTotalAmount.setText("Total Amount Requested : " + liveRequestedTotal);
                approvedTotalAmount.setText("Total Amount After Approval : " + liveAdjustedTotal);

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
                        Notification.show(
                                        "Compliance Fault: Action rejected because the Department Budget ledger is missing.",
                                        4000, Position.TOP_CENTER);
                        return;
                }

                recalculateTotalApprovalEvaluationAmount();
                double finalAmount = getFinalCalculatedTotal();

                if (remainingBudget < finalAmount) {
                        Notification.show("Compliance Block: Total evaluated amount of " + finalAmount
                                        + " exceeds available Department Budget balance of " + remainingBudget + "!",
                                        5000, Position.TOP_CENTER);
                        return;
                }

                try {
                        Employee actingEmployeeGroupMember = securityService.getLoggedInUser().getEmployee();

                        for (PurchaseRequestLine approvedLine : workingLinesList) {
                                if (approvedLine.getApprovedQuantity() == 0) {
                                        approvedLine.setStatus(Status.REJECTED);
                                }
                                if (workingSchedulesMap.containsKey(approvedLine)) {
                                        RepeatedPeriod activeSchedule = workingSchedulesMap.get(approvedLine);
                                        activeSchedule.setReferType(RepeatedPeriodReferType.PURCHASE_REQUEST_LINE);
                                        activeSchedule.setReferId(approvedLine.getId());
                                        RepeatedPeriod savedPeriod = repeatedPeriodService
                                                        .addRepeatedPeriod(activeSchedule, actingEmployeeGroupMember);
                                        approvedLine.setRepeatableId(savedPeriod.getId());
                                }
                                lineService.updatePurchaseRequestLine(approvedLine, actingEmployeeGroupMember);

                                if (hasRepeatedPeriod.contains(approvedLine)) {
                                        if (workingSchedulesMap.containsKey(approvedLine)) {
                                                repeatedPeriodService.updateRepeatedPeriod(
                                                                workingSchedulesMap.get(approvedLine),
                                                                actingEmployeeGroupMember);
                                        } else {
                                                RepeatedPeriod period = repeatedPeriodService
                                                                .getRepeatedPeriodById(approvedLine.getRepeatableId())
                                                                .get();
                                                period.setStatus(RequestForQuotationStatus.CANCELLED);
                                                repeatedPeriodService.updateRepeatedPeriod(period,
                                                                actingEmployeeGroupMember);
                                        }
                                }
                        }

                        approval.setStatus(Status.APPROVED);
                        approval.setComments(comments.getValue() != null ? comments.getValue().trim()
                                        : "Approved by group.");
                        approval.setApprovedDate(LocalDate.now());
                        approval.setApprover(actingEmployeeGroupMember);

                        approvalsService.updateApprovals(approval, actingEmployeeGroupMember);

                        Notification.show("Task approved and line items updated successfully.", 3000,
                                        Position.TOP_CENTER);
                        getUI().ifPresent(ui -> ui.navigate("purchase-request"));

                } catch (Exception exception) {
                        Notification.show("Error processing approval transaction: " + exception.getMessage(), 5000,
                                        Position.MIDDLE);
                }
        }

        private void rejectRequest() {
                if (comments.isEmpty()) {
                        Notification.show("Rejection requires descriptive remarks input inside comments field.", 3000,
                                        Position.TOP_CENTER);
                        return;
                }

                try {
                        Employee actingEmployeeGroupMember = securityService.getLoggedInUser().getEmployee();

                        for (PurchaseRequestLine line : workingLinesList) {
                                line.setApprovedQuantity(0.0);
                                line.setItemTotalAmount(0.0);
                                line.setStatus(Status.REJECTED);
                                lineService.updatePurchaseRequestLine(line, actingEmployeeGroupMember);
                                if (hasRepeatedPeriod.contains(line)) {
                                        if (workingSchedulesMap.containsKey(line)) {
                                                repeatedPeriodService.updateRepeatedPeriod(
                                                                workingSchedulesMap.get(line),
                                                                actingEmployeeGroupMember);
                                        } else {
                                                RepeatedPeriod period = repeatedPeriodService
                                                                .getRepeatedPeriodById(line.getRepeatableId()).get();
                                                period.setStatus(RequestForQuotationStatus.CANCELLED);
                                                repeatedPeriodService.updateRepeatedPeriod(period,
                                                                actingEmployeeGroupMember);
                                        }
                                }
                        }

                        approval.setStatus(Status.REJECTED);
                        approval.setComments(comments.getValue().trim());
                        approval.setApprovedDate(LocalDate.now());
                        approval.setApprover(actingEmployeeGroupMember);

                        approvalsService.updateApprovals(approval, actingEmployeeGroupMember);

                        Notification.show("Purchase request verification rejected successfully.", 3000,
                                        Position.TOP_CENTER);
                        getUI().ifPresent(ui -> ui.navigate("purchase-request"));

                } catch (Exception exception) {
                        Notification.show("Error processing rejection workflow: " + exception.getMessage(), 5000,
                                        Position.MIDDLE);
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
                                com.vaadin.flow.component.html.Image image = new com.vaadin.flow.component.html.Image(
                                                url, document.getFileName());
                                image.setWidthFull();
                                image.setMaxHeight("75vh");
                                image.getStyle().set("object-fit", "contain");
                                dialogContent.add(closeButton, image);
                        } else if ("application/pdf".equals(fileType)) {
                                com.vaadin.flow.component.Html pdfViewer = new com.vaadin.flow.component.Html(
                                                "<object data='" + url
                                                                + "' type='application/pdf' width='100%' height='100%' style='min-height:70vh;'></object>");
                                dialogContent.add(closeButton, pdfViewer);
                        } else {
                                Button downloadButton = new Button("Download Document Attachment");
                                downloadButton.addThemeName("success");
                                downloadButton.addClickListener(e -> ui.getPage().open(url, "_blank"));

                                dialogContent.add(closeButton, new Span(
                                                "Inline display rendering engine is unavailable for this format extension."),
                                                downloadButton);
                        }

                        previewDialog.add(dialogContent);
                        previewDialog.open();
                });
        }
}