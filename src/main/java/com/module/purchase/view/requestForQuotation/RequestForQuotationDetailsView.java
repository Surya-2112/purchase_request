package com.module.purchase.view.requestForQuotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.QuotationService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "request-for-quotation-details", layout = MainLayout.class)
@PermitAll
public class RequestForQuotationDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final RequestForQuotationService rfqService;
    private final PurchaseRequestLineService prLineService;
    private final QuotationService quotationService;
    private final SecurityService securityService;

    private RequestForQuotation currentRfq;
    private boolean isVendorUser = false;
    private Vendor loggedInVendor;

    private final TextField rfqIdField = new TextField("RFQ Reference ID");
    private final TextField requestedDateField = new TextField("Requested Date");

    private final DatePicker requestEndDateField = new DatePicker("Quotation Closing / End Date");
    private final Button updateDateBtn = new Button("Extend / Adjust Closing Date");
    private final HorizontalLayout statusBadgeContainer = new HorizontalLayout();

    private final Grid<RequestForQuotationLine> detailsLinesGrid = new Grid<>(RequestForQuotationLine.class, false);
    private final List<RequestForQuotationLine> linesDataset = new ArrayList<>();

    private final Button backBtn = new Button("Back to Dashboard");
    private final Button editBtn = new Button("Edit Draft Layout");
    private final Button closeRfqBtn = new Button("Close RFQ");
    private final Button cancelRfqBtn = new Button("Cancel RFQ");
    private final Button addQuotationBtn = new Button("Add Quotation Bid");
    private final Button createQuotationBtn = new Button("Submit Quotation Bid");

    public RequestForQuotationDetailsView(RequestForQuotationService rfqService,
            PurchaseRequestLineService prLineService,
            QuotationService quotationService,
            SecurityService securityService) {
        this.rfqService = rfqService;
        this.prLineService = prLineService;
        this.quotationService = quotationService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        evaluateUserRoleContext();
        buildUI();
    }

    private void evaluateUserRoleContext() {
        if (securityService.getLoggedInUser() != null && securityService.getLoggedInUser().getVendor() != null) {
            this.isVendorUser = true;
            this.loggedInVendor = securityService.getLoggedInUser().getVendor();
        }
    }

    private void buildUI() {
        VerticalLayout scrollContent = new VerticalLayout();
        scrollContent.setWidthFull();
        scrollContent.setPadding(true);
        scrollContent.setSpacing(true);

        H2 pageTitle = new H2("Request for Quotation");

        rfqIdField.setReadOnly(true);
        requestedDateField.setReadOnly(true);
        requestEndDateField.setReadOnly(true);
        requestEndDateField.setMin(LocalDate.now().plusDays(1));

        FormLayout headerLayout = new FormLayout(rfqIdField, requestedDateField, requestEndDateField);
        headerLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        updateDateBtn.addThemeName("primary small");
        updateDateBtn.setIcon(VaadinIcon.CHECK.create());
        updateDateBtn.setVisible(false);
        updateDateBtn.addClickListener(e -> saveClosingDateExtension());

        HorizontalLayout dateAdjustmentRow = new HorizontalLayout(updateDateBtn);
        dateAdjustmentRow.setPadding(false);

        statusBadgeContainer.setAlignItems(Alignment.CENTER);
        HorizontalLayout statusSection = new HorizontalLayout(new Span("Status: "), statusBadgeContainer);
        statusSection.setAlignItems(Alignment.CENTER);

        detailsLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName()
                : "").setHeader("Item").setAutoWidth(true);
        detailsLinesGrid
                .addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification Detail").setAutoWidth(true);
        detailsLinesGrid.addColumn(RequestForQuotationLine::getRequestedQuantity).setHeader("Quantity")
                .setWidth("160px");

        detailsLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        detailsLinesGrid.setAllRowsVisible(true);
        detailsLinesGrid.setWidthFull();

        backBtn.addClickListener(e -> backToDashboard());

        editBtn.addThemeName("primary warning");
        editBtn.setVisible(false);

        closeRfqBtn.addThemeName("primary success");
        closeRfqBtn.setVisible(false);
        closeRfqBtn.addClickListener(e -> executeCloseRfqRoutine());

        cancelRfqBtn.addThemeName("error primary");
        cancelRfqBtn.setVisible(false);
        cancelRfqBtn.addClickListener(e -> executeCancelRfqRoutine());

        addQuotationBtn.addThemeName("primary success");
        addQuotationBtn.setVisible(false);
        addQuotationBtn.addClickListener(e -> navigateToQuotationForm());

        createQuotationBtn.addThemeName("primary success");
        createQuotationBtn.setVisible(false);
        createQuotationBtn.addClickListener(e -> navigateToQuotationForm());

        HorizontalLayout actionsLayout = new HorizontalLayout(backBtn, editBtn, closeRfqBtn, cancelRfqBtn,
                addQuotationBtn, createQuotationBtn);
        actionsLayout.setSpacing(true);

        scrollContent.add(pageTitle, headerLayout, dateAdjustmentRow, statusSection, new Hr(),
                new H3("Request for Quotations Items"), detailsLinesGrid, actionsLayout);

        Scroller scroller = new Scroller(scrollContent);
        scroller.setSizeFull();
        add(scroller);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id == null) {
            backToDashboard();
            return;
        }

        try {
            rfqService.getRequestForQuotationById(id).ifPresentOrElse(rfq -> {
                this.currentRfq = rfq;

                rfqIdField.setValue("RFQ-" + rfq.getId());
                requestedDateField.setValue(rfq.getRequestedDate() != null ? rfq.getRequestedDate().toString() : "-");
                requestEndDateField.setValue(rfq.getRequestEndDate());

                renderStatusBadge(rfq.getStatus());

                linesDataset.clear();
                linesDataset.addAll(rfqService.getLinesByRfqId(rfq.getId()));
                detailsLinesGrid.setItems(linesDataset);

                if (isVendorUser) {
                    editBtn.setVisible(false);
                    closeRfqBtn.setVisible(false);
                    cancelRfqBtn.setVisible(false);
                    addQuotationBtn.setVisible(false);
                    updateDateBtn.setVisible(false);
                    requestEndDateField.setReadOnly(true);

                    boolean rfqOpen = rfq.getStatus().equals(RequestForQuotationStatus.OPEN);
                    createQuotationBtn.setVisible(rfqOpen);
                    if (!rfqOpen ) {
                        event.forwardTo("request-for-quotation");
                        event.getUI().access(
                            () -> Notification.show("Requested Quotation is not Open.", 4000, Position.MIDDLE));
                    }
                    boolean asCategory=false;
                    for(Category c :loggedInVendor.getCategories())
                    {
                        if(c.getCategoryId().equals(rfq.getCategory().getCategoryId()))
                        {
                            asCategory=true;
                        }
                    }
                    if(!asCategory)
                    {
                        event.forwardTo("request-for-quotation");
                        event.getUI().access(
                            () -> Notification.show("Requested Quotation Not Yours.", 4000, Position.MIDDLE));
                    }

                } else {
                    createQuotationBtn.setVisible(false);

                    switch (rfq.getStatus()) {
                        case DRAFT -> {
                            editBtn.setVisible(true);
                            editBtn.addClickListener(
                                    click -> getUI().ifPresent(ui -> ui.navigate("rfq-form/" + rfq.getId())));
                            closeRfqBtn.setVisible(false);
                            cancelRfqBtn.setVisible(false);
                            addQuotationBtn.setVisible(false);
                            requestEndDateField.setReadOnly(true);
                            updateDateBtn.setVisible(false);
                        }
                        case OPEN -> {
                            editBtn.setVisible(false);
                            closeRfqBtn.setVisible(true);
                            cancelRfqBtn.setVisible(true);
                            addQuotationBtn.setVisible(true);
                            requestEndDateField.setReadOnly(false);
                            updateDateBtn.setVisible(true);
                        }
                        default -> {
                            editBtn.setVisible(false);
                            closeRfqBtn.setVisible(false);
                            cancelRfqBtn.setVisible(false);
                            addQuotationBtn.setVisible(false);
                            requestEndDateField.setReadOnly(true);
                            updateDateBtn.setVisible(false);
                        }
                    }
                }

            }, () -> {
                event.forwardTo("request-for-quotation");
                event.getUI().access(() -> {
                    Notification.show("Requested profile data missing.", 4000, Position.MIDDLE);
                });
            });
        } catch (Exception ex) {
            event.forwardTo("request-for-quotation");
            event.getUI().access(() -> {
                Notification.show(ex.getMessage(), 4000, Position.MIDDLE);
            });
            return;

        }
    }

    private void navigateToQuotationForm() {
        if (this.currentRfq != null) {
            getUI().ifPresent(ui -> ui.navigate("quotation-form/new/" + currentRfq.getId()));
        }
    }

    private void saveClosingDateExtension() {
        if (requestEndDateField.isEmpty()) {
            Notification.show("Please enter a valid timeline closure date target.", 3000, Position.MIDDLE);
            return;
        }
        try {
            Employee executionActor = securityService.getLoggedInUser().getEmployee();
            currentRfq.setRequestEndDate(requestEndDateField.getValue());
            rfqService.updateRequestForQuotation(currentRfq, executionActor);

            Notification.show("RFQ Bidding closure extension saved successfully!", 3000, Position.TOP_CENTER);
            backToDashboard();
        } catch (Exception ex) {
            Notification.show("Database transaction rejected: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void executeCloseRfqRoutine() {
        if (this.currentRfq == null || this.currentRfq.getId() == null)
            return;

        try {
            Employee actor = securityService.getLoggedInUser().getEmployee();

            currentRfq.setRequestEndDate(LocalDate.now());
            currentRfq.setStatus(RequestForQuotationStatus.CLOSED);

            rfqService.updateRequestForQuotation(currentRfq, actor);

            Notification.show("RFQ Timeline frozen. Document has been securely marked as CLOSED.", 4000,
                    Position.TOP_CENTER);
            backToDashboard();

        } catch (Exception ex) {
            Notification.show("Closing pipeline routine hit an issue: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void executeCancelRfqRoutine() {
        if (this.currentRfq == null || this.currentRfq.getId() == null)
            return;

        try {
            Employee actor = securityService.getLoggedInUser().getEmployee();

            List<PurchaseRequestLine> connectedPrLines = prLineService.getRequestForQuotation(currentRfq);
            for (PurchaseRequestLine prLine : connectedPrLines) {
                prLine.setRequestForQuotation(null);
                prLineService.updatePurchaseRequestLine(prLine, actor);
            }

            List<Quotation> associatedQuotations = quotationService.getQuotationsByRfq(currentRfq);
            for (Quotation quotation : associatedQuotations) {
                if (quotation.getStatus() != Status.REJECTED) {
                    quotation.setStatus(Status.CANCELLED);
                    quotationService.updateQuotation(quotation);
                }
            }

            currentRfq.setStatus(RequestForQuotationStatus.CANCELLED);
            rfqService.updateRequestForQuotation(currentRfq, actor);

            Notification.show("Request for Quotation successfully cancelled. Demands unmapped safely.", 4000,
                    Position.TOP_CENTER);
            backToDashboard();

        } catch (Exception ex) {
            Notification.show("Cancellation pipeline encountered an error: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void renderStatusBadge(RequestForQuotationStatus status) {
        statusBadgeContainer.removeAll();
        Span badge = new Span(status != null ? status.name() : "UNKNOWN");
        badge.getStyle()
                .set("padding", "4px 12px").set("border-radius", "12px")
                .set("font-weight", "bold").set("font-size", "13px");

        if (status == RequestForQuotationStatus.DRAFT) {
            badge.getStyle().set("background-color", "#f1f5f9").set("color", "#475569");
        } else if (status == RequestForQuotationStatus.OPEN) {
            badge.getStyle().set("background-color", "#e0f2fe").set("color", "#0369a1");
        } else if (status == RequestForQuotationStatus.CLOSED) {
            badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
        } else if (status == RequestForQuotationStatus.CANCELLED) {
            badge.getStyle().set("background-color", "#fef3c7").set("color", "#d97706");
        }
        statusBadgeContainer.add(badge);
    }

    private void backToDashboard() {
        if (isVendorUser) {
            getUI().ifPresent(ui -> ui.navigate("vendor-sourcing"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation"));
        }
    }
}