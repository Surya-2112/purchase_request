package com.module.purchase.view.quotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.QuotationService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.service.VendorService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;

import jakarta.annotation.security.PermitAll;

@Route(value = "quotation-form", layout = MainLayout.class)
@PermitAll
public class QuotationFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final RequestForQuotationService rfqService;
    private final QuotationService quotationService;
    private final VendorService vendorService;
    private final SecurityService securityService;

    private Quotation existingQuotation;
    private RequestForQuotation targetRfq;
    private Vendor selectedVendorContext;
    private boolean isVendorUser = false;
    private final H2 pageTitle = new H2("Quotation Pricing Proposal");
    private final TextField rfqIdField = new TextField("RFQ Reference");
    private final ComboBox<Vendor> vendorSelector = new ComboBox<>("Select Supplying Vendor *");
    private final TextField quotationDateField = new TextField("Quotation Date");
    private final NumberField totalAmountField = new NumberField("Calculated Total Amount");
    private final NumberField totalDiscountField = new NumberField("Total Discount Amount");
    private final NumberField totalAmountAfterDiscountField = new NumberField("Total Amount After Discount");

    private final Grid<QuotationLine> biddingGrid = new Grid<>();
    private final List<QuotationLine> biddingDataset = new ArrayList<>();

    private final Button saveDraftBtn = new Button("Save as Draft");
    private final Button submitBidBtn = new Button("Submit Final Bid");
    private final Button cancelBtn = new Button("Cancel");

    public QuotationFormView(RequestForQuotationService rfqService, QuotationService quotationService,
            VendorService vendorService, SecurityService securityService) {
        this.rfqService = rfqService;
        this.quotationService = quotationService;
        this.vendorService = vendorService;
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
            this.selectedVendorContext = securityService.getLoggedInUser().getVendor();
        }
    }

    private void buildUI() {
        VerticalLayout scrollContent = new VerticalLayout();
        scrollContent.setWidthFull();
        scrollContent.setPadding(true);
        scrollContent.setSpacing(true);

        rfqIdField.setReadOnly(true);
        quotationDateField.setValue(LocalDate.now().toString());
        quotationDateField.setReadOnly(true);

        totalAmountField.setValue(0.0);
        totalAmountField.setReadOnly(true);

        totalDiscountField.setValue(0.0);
        totalDiscountField.setReadOnly(true);

        totalAmountAfterDiscountField.setValue(0.0);
        totalAmountAfterDiscountField.setReadOnly(true);

        if (isVendorUser) {
            vendorSelector.setVisible(false);
        } else {
            vendorSelector.setItemLabelGenerator(Vendor::getVendorName);
            vendorSelector.setPlaceholder("Vendor ...");
            vendorSelector.setRequired(true);
            vendorSelector.setVisible(true);
            vendorSelector.addValueChangeListener(event -> {
                this.selectedVendorContext = event.getValue();
                checkSubmissionComplianceGuard();
            });
        }

        FormLayout summaryFormLayout = new FormLayout(rfqIdField, vendorSelector, quotationDateField, totalAmountField,
                totalDiscountField, totalAmountAfterDiscountField);
        summaryFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));

        biddingGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName()
                : "").setHeader("Item Name").setAutoWidth(true);

        biddingGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification Requirement").setAutoWidth(true);

        biddingGrid.addColumn(line -> {
            if (targetRfq != null && line.getItemVariant() != null) {
                return rfqService.getLinesByRfqId(targetRfq.getId()).stream()
                        .filter(rfqLine -> rfqLine.getItemVariant() != null &&
                                rfqLine.getItemVariant().getId().equals(line.getItemVariant().getId()))
                        .map(RequestForQuotationLine::getRequestedQuantity)
                        .findFirst()
                        .orElse(0.0);
            }
            return 0.0;
        }).setHeader("Quantity Needed").setWidth("140px");

        biddingGrid.addComponentColumn(line -> {
            NumberField unitPriceInput = new NumberField();
            unitPriceInput.setValue(line.getUnitPrice());
            unitPriceInput.setMin(0.01);
            unitPriceInput.setPlaceholder("Enter Base Price...");
            unitPriceInput.setWidth("160px");
            unitPriceInput.addValueChangeListener(change -> {
                line.setUnitPrice(change.getValue() != null ? change.getValue() : 0.0);
                recalculateGrossTotalAmount();
            });
            return unitPriceInput;
        }).setHeader("Unit Price ").setWidth("180px");

        biddingGrid.addComponentColumn(line -> {
            NumberField discountPercentage = new NumberField();
            discountPercentage.setValue(line.getDiscount());
            discountPercentage.setMin(0);
            discountPercentage.setMax(100);
            discountPercentage.setPlaceholder("Enter Discount Percentage");
            discountPercentage.addValueChangeListener(change -> {
                line.setDiscount(change.getValue() != null ? change.getValue() : 0.0);
                recalculateGrossTotalAmount();
            });
            return discountPercentage;
        }).setHeader("Discount percentage").setWidth("180px");

        biddingGrid.setAllRowsVisible(true);
        biddingGrid.setWidthFull();
        biddingGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        saveDraftBtn.addThemeName("secondary contrast");
        saveDraftBtn.addClickListener(e -> processTransactionCommit(Status.DRAFT));

        submitBidBtn.addThemeName("primary success");
        submitBidBtn.addClickListener(e -> processTransactionCommit(Status.WAITING_APPROVAL));

        cancelBtn.addThemeName("tertiary error");
        cancelBtn.addClickListener(e -> backToDashboard());

        HorizontalLayout actionsLayout = new HorizontalLayout(saveDraftBtn, submitBidBtn, cancelBtn);
        actionsLayout.setSpacing(true);

        scrollContent.add(pageTitle, summaryFormLayout, new Hr(),
                new H3("Item Specifications & Price Entry Sheet"), biddingGrid,
                new Hr(), actionsLayout);

        Scroller viewScroller = new Scroller(scrollContent);
        viewScroller.setSizeFull();
        add(viewScroller);
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        if (parameter == null) {
            event.forwardTo("request-for-quotation");
            event.getUI().access(() -> {
                Notification.show("Invalid route context pathway configuration.", 3000, Position.MIDDLE);
            });
            return;
        }

        biddingDataset.clear();
        this.existingQuotation = null;

        if (parameter.startsWith("new/")) {
            pageTitle.setText("New Quotation Pricing Proposal");
            String rfqIdStr = parameter.substring(4).trim();
            try {
                Long rfqId = Long.valueOf(rfqIdStr);
                rfqService.getRequestForQuotationById(rfqId).ifPresentOrElse(rfq -> {
                    this.targetRfq = rfq;
                    rfqIdField.setValue("RFQ REFERENCE #" + rfq.getId());

                    if (!rfq.getStatus().equals(RequestForQuotationStatus.OPEN)) {
                        event.forwardTo("request-for-quotation");
                        event.getUI().access(() -> {
                            Notification.show("Request For Quotation is not Open", 3000, Position.MIDDLE);
                        });
                        return;

                    }
                    if (!isVendorUser) {
                        List<RequestForQuotationLine> requiredLines = rfqService.getLinesByRfqId(rfq.getId());
                        if (!requiredLines.isEmpty()) {
                            Category rfqCategory = requiredLines.get(0).getItemVariant().getItem().getCategory();
                            vendorSelector.setItems(vendorService.getVendorsByCategory(rfqCategory));
                        }
                    } else {
                        checkSubmissionComplianceGuard();
                    }

                    for (RequestForQuotationLine rfqLine : rfqService.getLinesByRfqId(rfq.getId())) {
                        QuotationLine pricingRow = new QuotationLine();
                        pricingRow.setItemVariant(rfqLine.getItemVariant());
                        pricingRow.setUnitPrice(0.0);
                        pricingRow.setRequestForQuotationLine(rfqLine);
                        biddingDataset.add(pricingRow);
                    }
                    biddingGrid.setItems(biddingDataset);
                }, () -> {
                    Notification.show("RFQ record data is missing.", 4000, Position.MIDDLE);
                    backToDashboard();
                });
            } catch (NumberFormatException e) {
                event.forwardTo(ViewName.REQUEST_FOR_QUOTATION.getRoute());
                event.getUI().access(() -> {
                    Notification.show("url is not valid ," + e.getMessage(), 3000,
                            Notification.Position.TOP_CENTER);
                });
                return;
            } catch (Exception ex) { 
                event.forwardTo(ViewName.REQUEST_FOR_QUOTATION.getRoute());
                event.getUI().access(() -> {
                    Notification.show(ex.getMessage(), 3000, Position.MIDDLE);
                });
                return;
            }

        } else if (parameter.startsWith("edit/")) {
            pageTitle.setText("Modify Quotation Draft Workspace");
            String quotationIdStr = parameter.substring(5).trim();
            try {
                Long quoteId = Long.valueOf(quotationIdStr);
                quotationService.getQuotationById(quoteId).ifPresentOrElse(quote -> {
                    this.existingQuotation = quote;
                    this.targetRfq = quote.getRequestForQuotation();

                    if (isVendorUser && !quote.getVendor().getVendorId().equals(selectedVendorContext.getVendorId())) {
                        event.forwardTo("quotations");
                        event.getUI().access(() -> {
                            Notification.show("This is not your Quotations", 3000, Position.MIDDLE);
                        });
                    }

                    if (!quote.getStatus().equals(Status.DRAFT)) {
                        event.forwardTo("quotations");
                        event.getUI().access(() -> {
                            Notification.show("Quotations is Finalized", 3000, Position.MIDDLE);
                        });
                        return;
                    }

                    rfqIdField.setValue(targetRfq != null ? "RFQ REFERENCE #" + targetRfq.getId() : "-");
                    quotationDateField
                            .setValue(quote.getQuotationDate() != null ? quote.getQuotationDate().toString() : "-");
                    totalAmountField.setValue(quote.getTotalAmount());

                    if (!isVendorUser) {
                        vendorSelector.setItems(quote.getVendor());
                        vendorSelector.setValue(quote.getVendor());
                        vendorSelector.setReadOnly(true);
                    }

                    if (!quote.getRequestForQuotation().getStatus().equals(RequestForQuotationStatus.OPEN)) {
                        saveDraftBtn.setEnabled(false);
                        submitBidBtn.setEnabled(false);
                        Notification.show("Request For Quotation is closed ", 4000, Position.MIDDLE);
                    } else {
                        saveDraftBtn.setEnabled(true);
                        submitBidBtn.setEnabled(true);
                    }

                    List<QuotationLine> savedLines = quotationService.getLinesByQuotation(quote);
                    for (QuotationLine line : savedLines) {
                        biddingDataset.add(line);
                    }

                    biddingGrid.setItems(biddingDataset);
                    recalculateGrossTotalAmount();
                }, () -> {
                    Notification.show("Quotation draft missing or unmapped in storage registry.", 4000,
                            Position.MIDDLE);
                    backToDashboard();
                });
            } catch (Exception ex) {
                event.forwardTo("quotations");
                event.getUI().access(() -> {
                    Notification.show(ex.getMessage(), 3000, Position.MIDDLE);
                });
                return;
            }
        } else {
            Notification.show("Route signature pattern rejected.", 3000, Position.MIDDLE);
            backToDashboard();
        }
    }

    private void checkSubmissionComplianceGuard() {
        if (existingQuotation == null && targetRfq != null && selectedVendorContext != null) {
            if (quotationService.isDuplicateSubmission(targetRfq.getId(), selectedVendorContext.getVendorId())) {
                Notification.show("Compliance Block: Vendor '" + selectedVendorContext.getVendorName()
                        + "' already holds a pricing submission for RFQ-" + targetRfq.getId() + ".", 5000,
                        Position.MIDDLE);
                saveDraftBtn.setEnabled(false);
                submitBidBtn.setEnabled(false);
            } else {
                saveDraftBtn.setEnabled(true);
                submitBidBtn.setEnabled(true);
            }
        }
    }

    private void recalculateGrossTotalAmount() {
        if (targetRfq == null) {
            totalAmountField.setValue(0.0);
            totalDiscountField.setValue(0.0);
            totalAmountAfterDiscountField.setValue(0.0);
            return;
        }

        double currentRunningSum = 0.0;
        double currentDiscount = 0.0;
        for (QuotationLine pricingLine : biddingDataset) {
            if (pricingLine.getItemVariant() != null) {
                double qty = 0;
                qty = pricingLine.getRequestForQuotationLine().getRequestedQuantity();
                double price = pricingLine.getUnitPrice() != null ? pricingLine.getUnitPrice() : 0.0;
                double discount = pricingLine.getDiscount() != null ? pricingLine.getDiscount() : 0.0;
                currentRunningSum += (qty * price);
                currentDiscount += (qty * ((price * discount / 100)));
            }
        }
        totalAmountField.setValue(currentRunningSum);
        totalDiscountField.setValue(currentDiscount);
        totalAmountAfterDiscountField.setValue(currentRunningSum - currentDiscount);
    }

    private void processTransactionCommit(Status targetedLifecycleState) {
        if (selectedVendorContext == null) {
            Notification.show("Validation Block: Please specify the supplying vendor owner.", 4000, Position.MIDDLE);
            return;
        }

        if (existingQuotation == null
                && quotationService.isDuplicateSubmission(targetRfq.getId(), selectedVendorContext.getVendorId())) {
            Notification.show("Transaction Cancelled: Unique contract constraints matching hit.", 4000,
                    Position.MIDDLE);
            return;
        }

        for (QuotationLine line : biddingDataset) {
            if (line.getUnitPrice() <= 0) {
                Notification.show(
                        "Validation Fault: Every requested item variant line requires a valid, positive base price.",
                        4000, Position.MIDDLE);
                return;
            }
        }

        try {
            Quotation masterProposal = (existingQuotation != null) ? existingQuotation : new Quotation();
            masterProposal.setRequestForQuotation(targetRfq);
            masterProposal.setVendor(selectedVendorContext);
            masterProposal.setQuotationDate(LocalDate.now());
            masterProposal.setTotalAmount(totalAmountField.getValue());
            masterProposal.setStatus(targetedLifecycleState);

            if (existingQuotation != null) {
                quotationService.updateQuotation(masterProposal);
            } else {
                masterProposal = quotationService.addQuotation(masterProposal);
            }

            for (QuotationLine pricingLine : biddingDataset) {
                pricingLine.setQuotation(masterProposal);
                QuotationLine savedLine = quotationService.saveQuotationLine(pricingLine);
            }

            Notification.show(
                    "Financial proposal workflow successfully saved under status: " + targetedLifecycleState.name(),
                    3000, Position.TOP_CENTER);
            backToDashboard();

        } catch (Exception ex) {
            Notification.show("Database transaction rejected: Sourcing pipelines write failure -> " + ex.getMessage(),
                    5000, Position.MIDDLE);
        }
    }

    private void backToDashboard() {
        getUI().ifPresent(ui -> ui.navigate("request-for-quotation"));
    }
}