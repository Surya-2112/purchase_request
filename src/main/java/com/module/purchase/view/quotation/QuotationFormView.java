package com.module.purchase.view.quotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.entity.Vendor;
import com.module.purchase.enums.Status;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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

    private final QuotationService quotationService;
    private final RequestForQuotationService rfqService;
    private final VendorService vendorService;
    private final SecurityService securityService;

    private RequestForQuotation targetRfq;
    private Vendor selectedVendorContext;
    private boolean isVendorUser = false;
    private QuotationLineWrapper activeSelectedLine; // Track row clicks for discount mapping

    // Master Header Form Layout Mappings
    private final TextField rfqIdField = new TextField("Target RFQ Reference");
    private final ComboBox<Vendor> vendorSelector = new ComboBox<>("Select Supplying Vendor *");
    private final TextField quotationDateField = new TextField("Quotation Date");
    private final NumberField totalAmountField = new NumberField("Calculated Gross Total Amount");

    // Live Master Bidding Worksheet Grid Layout
    private final Grid<QuotationLineWrapper> biddingGrid = new Grid<>();
    private final List<QuotationLineWrapper> biddingDataset = new ArrayList<>();

    // Live Detail Slab Discount Pricing Matrix Layout components
    private final VerticalLayout discountSubPanel = new VerticalLayout();
    private final Span discountPanelHeader = new Span("Select an item above to map tiered volume break discounts.");
    private final Grid<DiscountWrapper> discountMatrixGrid = new Grid<>();
    private final Button addSlabRowBtn = new Button("Add Discount Slab Tier Row");

    // Action Row Buttons
    private final Button saveDraftBtn = new Button("Save as Draft");
    private final Button submitBidBtn = new Button("Submit Final Bid");
    private final Button cancelBtn = new Button("Cancel");

    public QuotationFormView(QuotationService quotationService,
                             RequestForQuotationService rfqService,
                             VendorService vendorService,
                             SecurityService securityService) {
        this.quotationService = quotationService;
        this.rfqService = rfqService;
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

        H2 pageTitle = new H2("Compile Quotation Pricing Proposal");

        rfqIdField.setReadOnly(true);
        quotationDateField.setValue(LocalDate.now().toString());
        quotationDateField.setReadOnly(true);
        
        totalAmountField.setValue(0.0);
        totalAmountField.setReadOnly(true);
        totalAmountField.setSuffixComponent(new Span("INR"));

        if (isVendorUser) {
            vendorSelector.setVisible(false);
        } else {
            vendorSelector.setItems(vendorService.getVendors());
            vendorSelector.setItemLabelGenerator(Vendor::getVendorName);
            vendorSelector.setPlaceholder("Assign bid owner...");
            vendorSelector.setRequired(true);
            vendorSelector.setVisible(true);
            vendorSelector.addValueChangeListener(event -> {
                this.selectedVendorContext = event.getValue();
                checkSubmissionComplianceGuard();
            });
        }

        FormLayout summaryFormLayout = new FormLayout(rfqIdField, vendorSelector, quotationDateField, totalAmountField);
        summaryFormLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));

        // 1. CONFIGURE WORKSHEET BASE PRICING GRID
        biddingGrid.addColumn(w -> w.getRfqLine().getItemVariant() != null && w.getRfqLine().getItemVariant().getItem() != null
                ? w.getRfqLine().getItemVariant().getItem().getItemName() : "").setHeader("Item Name").setAutoWidth(true);
        biddingGrid.addColumn(w -> w.getRfqLine().getItemVariant() != null ? w.getRfqLine().getItemVariant().getSpecification() : "")
                .setHeader("Specification Requirement").setAutoWidth(true);
        biddingGrid.addColumn(w -> w.getRfqLine().getRequestedQuantity()).setHeader("Quantity Needed").setWidth("140px");

        biddingGrid.addComponentColumn(w -> {
            NumberField unitPriceInput = new NumberField();
            unitPriceInput.setValue(w.getUnitPrice());
            unitPriceInput.setMin(0.01);
            unitPriceInput.setPlaceholder("Enter Base Price...");
            unitPriceInput.setWidth("160px");
            unitPriceInput.addValueChangeListener(change -> {
                w.setUnitPrice(change.getValue() != null ? change.getValue() : 0.0);
                recalculateGrossTotalAmount(); 
            });
            return unitPriceInput;
        }).setHeader("Unit Price *").setWidth("180px");

        biddingGrid.setAllRowsVisible(true);
        biddingGrid.setWidthFull();
        biddingGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        // INTERACTIVE ROW SELECTION CHAIN: Populates sub-slabs matrix grid instantly on row selection click
        biddingGrid.addSelectionListener(selection -> {
            if (selection.getFirstSelectedItem().isPresent()) {
                populateDiscountSubPanel(selection.getFirstSelectedItem().get());
            } else {
                clearDiscountSubPanel();
            }
        });

        // 2. CONFIGURE NESTED LIVE-EDITABLE DISCOUNT MATRIX SUB-GRID
        discountPanelHeader.getStyle().set("font-weight", "bold").set("color", "var(--lumo-secondary-text-color)");
        
        discountMatrixGrid.addComponentColumn(d -> {
            NumberField fromQtyInput = new NumberField();
            fromQtyInput.setValue(d.getFromQuantity());
            fromQtyInput.setMin(0.0);
            fromQtyInput.setPlaceholder("Min Qty...");
            fromQtyInput.addValueChangeListener(c -> d.setFromQuantity(c.getValue() != null ? c.getValue() : 0.0));
            return fromQtyInput;
        }).setHeader("From Quantity").setWidth("160px");

        discountMatrixGrid.addComponentColumn(d -> {
            NumberField toQtyInput = new NumberField();
            toQtyInput.setValue(d.getToQuantity());
            toQtyInput.setMin(0.1);
            toQtyInput.setPlaceholder("Max Qty...");
            toQtyInput.addValueChangeListener(c -> d.setToQuantity(c.getValue() != null ? c.getValue() : 0.0));
            return toQtyInput;
        }).setHeader("To Quantity").setWidth("160px");

        discountMatrixGrid.addComponentColumn(d -> {
            NumberField pctInput = new NumberField();
            pctInput.setValue(d.getDiscountPercentage());
            pctInput.setMin(0.0);
            pctInput.setMax(100.0);
            pctInput.setSuffixComponent(new Span("%"));
            pctInput.addValueChangeListener(c -> d.setDiscountPercentage(c.getValue() != null ? c.getValue() : 0.0));
            return pctInput;
        }).setHeader("Discount Percentage").setWidth("180px");

        discountMatrixGrid.addComponentColumn(d -> {
            Button rowTrashBtn = new Button(VaadinIcon.TRASH.create());
            rowTrashBtn.addThemeName("error small tertiary");
            rowTrashBtn.addClickListener(click -> {
                if (activeSelectedLine != null) {
                    activeSelectedLine.getDiscountSlabs().remove(d);
                    discountMatrixGrid.getDataProvider().refreshAll();
                }
            });
            return rowTrashBtn;
        }).setHeader("Remove").setWidth("90px");

        discountMatrixGrid.setAllRowsVisible(true);
        discountMatrixGrid.setWidthFull();
        discountMatrixGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);

        addSlabRowBtn.addThemeName("primary small");
        addSlabRowBtn.setIcon(VaadinIcon.PLUS.create());
        addSlabRowBtn.addClickListener(e -> addBlankDiscountSlabRowEntry());

        discountSubPanel.setWidthFull();
        discountSubPanel.setPadding(false);
        discountSubPanel.add(discountPanelHeader, discountMatrixGrid, addSlabRowBtn);
        discountSubPanel.setVisible(false); // Hide until main row mapping gets targeted active indicators

        // 3. ACTION COMMITS
        saveDraftBtn.addThemeName("secondary contrast");
        saveDraftBtn.addClickListener(e -> processTransactionCommit(Status.DRAFT));

        submitBidBtn.addThemeName("primary success");
        submitBidBtn.setIcon(VaadinIcon.PAPERPLANE.create());
        submitBidBtn.addClickListener(e -> processTransactionCommit(Status.APPROVED));

        cancelBtn.addThemeName("tertiary error");
        cancelBtn.addClickListener(e -> backToDashboard());

        HorizontalLayout actionsLayout = new HorizontalLayout(saveDraftBtn, submitBidBtn, cancelBtn);
        actionsLayout.setSpacing(true);

        scrollContent.add(pageTitle, summaryFormLayout, new Hr(), 
                           new H3("Material Sourcing Specifications & Price Entry Sheet"), biddingGrid, 
                           new Hr(), new H3("Volume Break Tier Discount Matrix (Selected Line Item)"), discountSubPanel,
                           new Hr(), actionsLayout);

        Scroller viewScroller = new Scroller(scrollContent);
        viewScroller.setSizeFull();
        add(viewScroller);
    }

    private void populateDiscountSubPanel(QuotationLineWrapper wrapper) {
        this.activeSelectedLine = wrapper;
        discountPanelHeader.setText("Configuring tiered discount matrix parameters matching row variant: " + 
                (wrapper.getRfqLine().getItemVariant() != null ? wrapper.getRfqLine().getItemVariant().getItem().getItemName() : ""));
        discountMatrixGrid.setItems(wrapper.getDiscountSlabs());
        discountSubPanel.setVisible(true);
    }

    private void clearDiscountSubPanel() {
        this.activeSelectedLine = null;
        discountSubPanel.setVisible(false);
    }

    private void addBlankDiscountSlabRowEntry() {
        if (activeSelectedLine != null) {
            activeSelectedLine.getDiscountSlabs().add(new DiscountWrapper());
            discountMatrixGrid.getDataProvider().refreshAll();
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        if (parameter == null || !parameter.startsWith("new/")) {
            Notification.show("Invalid target route pathway mapped.", 3000, Position.MIDDLE);
            backToDashboard();
            return;
        }

        String rfqIdStr = parameter.substring(4).trim();
        try {
            Long rfqId = Long.valueOf(rfqIdStr);
            rfqService.getRequestForQuotationById(rfqId).ifPresentOrElse(rfq -> {
                this.targetRfq = rfq;
                rfqIdField.setValue("RFQ REFERENCE #" + rfq.getId());
                
                if (isVendorUser) {
                    checkSubmissionComplianceGuard();
                }

                biddingDataset.clear();
                List<RequestForQuotationLine> requiredLines = rfqService.getLinesByRfqId(rfq.getId());
                for (RequestForQuotationLine rfqLine : requiredLines) {
                    biddingDataset.add(new QuotationLineWrapper(rfqLine));
                }
                biddingGrid.setItems(biddingDataset);
                clearDiscountSubPanel();
                
            }, () -> {
                Notification.show("Targeted RFQ record data is missing.", 4000, Position.MIDDLE);
                backToDashboard();
            });
        } catch (NumberFormatException nfe) {
            Notification.show("Mal-formed numeric keys route parameters.", 3000, Position.MIDDLE);
            backToDashboard();
        }
    }

    private void checkSubmissionComplianceGuard() {
        if (targetRfq != null && selectedVendorContext != null) {
            if (quotationService.isDuplicateSubmission(targetRfq.getId(), selectedVendorContext.getVendorId())) {
                Notification.show("Compliance Block: Vendor '" + selectedVendorContext.getVendorName() 
                        + "' already holds a pricing submission for RFQ-" + targetRfq.getId() + ".", 5000, Position.MIDDLE);
                saveDraftBtn.setEnabled(false);
                submitBidBtn.setEnabled(false);
            } else {
                saveDraftBtn.setEnabled(true);
                submitBidBtn.setEnabled(true);
            }
        }
    }

    private void recalculateGrossTotalAmount() {
        double currentRunningSum = 0.0;
        for (QuotationLineWrapper lineWrapper : biddingDataset) {
            double qty = lineWrapper.getRfqLine().getRequestedQuantity();
            currentRunningSum += (qty * lineWrapper.getUnitPrice());
        }
        totalAmountField.setValue(currentRunningSum);
    }

    private void processTransactionCommit(Status targetedLifecycleState) {
        if (selectedVendorContext == null) {
            Notification.show("Validation Block: Please specify the supplying vendor owner.", 4000, Position.MIDDLE);
            return;
        }

        if (quotationService.isDuplicateSubmission(targetRfq.getId(), selectedVendorContext.getVendorId())) {
            Notification.show("Transaction Cancelled: Unique contract constraints matching hit.", 4000, Position.MIDDLE);
            return;
        }

        // Validate basic pricing bounds
        for (QuotationLineWrapper w : biddingDataset) {
            if (w.getUnitPrice() <= 0) {
                Notification.show("Validation Fault: Every requested item variant line requires a valid, positive base price.", 4000, Position.MIDDLE);
                return;
            }
            // Validate attached discount slab tiers logic bounds parameters
            for (DiscountWrapper d : w.getDiscountSlabs()) {
                if (d.getFromQuantity() >= d.getToQuantity()) {
                    Notification.show("Validation Fault: Volume slab start boundary must sit strictly below end limits.", 4000, Position.MIDDLE);
                    return;
                }
            }
        }

        try {
            // 1. Persist master top header row metrics snapshots
            Quotation masterProposal = new Quotation();
            masterProposal.setRequestForQuotation(targetRfq);
            masterProposal.setVendor(selectedVendorContext);
            masterProposal.setQuotationDate(LocalDate.now());
            masterProposal.setTotalAmount(totalAmountField.getValue());
            masterProposal.setStatus(targetedLifecycleState);

            masterProposal = quotationService.saveQuotation(masterProposal);

            // 2. Persist downstream child proposal item details lines
            for (QuotationLineWrapper lineWrapper : biddingDataset) {
                QuotationLine detailRow = new QuotationLine();
                detailRow.setQuotation(masterProposal);
                detailRow.setItemVariant(lineWrapper.getRfqLine().getItemVariant());
                detailRow.setUnitPrice(lineWrapper.getUnitPrice());

                detailRow = quotationService.saveQuotationLine(detailRow);

                // 3. Persist nested cascading multi-tier volume break slab discounts mappings logs
                for (DiscountWrapper slab : lineWrapper.getDiscountSlabs()) {
                    DiscountType slabRow = new DiscountType();
                    slabRow.setQuotationLine(detailRow);
                    slabRow.setFromQuantity(slab.getFromQuantity());
                    slabRow.setToQuantity(slab.getToQuantity());
                    slabRow.setDiscountPercentage(slab.getDiscountPercentage());

                    quotationService.saveDiscountType(slabRow);
                }
            }

            Notification.show("Financial proposal workflow successfully saved under status: " + targetedLifecycleState.name(), 3000, Position.TOP_CENTER);
            backToDashboard();

        } catch (Exception ex) {
            Notification.show("Database transaction rejected: Sourcing pipelines write failure -> " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void backToDashboard() {
        if (isVendorUser) {
            getUI().ifPresent(ui -> ui.navigate("vendor-sourcing"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation"));
        }
    }

    // =========================================================================
    // WRAPPER WRAPPERS DTO ENTITIES LOGIC FOR TRANSIENT VALUE CAPTURE TRACKING
    // =========================================================================
    public static class QuotationLineWrapper {
        private final RequestForQuotationLine rfqLine;
        private double unitPrice = 0.0;
        private final List<DiscountWrapper> discountSlabs = new ArrayList<>();

        public QuotationLineWrapper(RequestForQuotationLine rfqLine) {
            this.rfqLine = rfqLine;
        }

        public RequestForQuotationLine getRfqLine() { return rfqLine; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
        public List<DiscountWrapper> getDiscountSlabs() { return discountSlabs; }
    }

    public static class DiscountWrapper {
        private double fromQuantity = 0.0;
        private double toQuantity = 0.0;
        private double discountPercentage = 0.0;

        public double getFromQuantity() { return fromQuantity; }
        public void setFromQuantity(double fromQuantity) { this.fromQuantity = fromQuantity; }
        public double getToQuantity() { return toQuantity; }
        public void setToQuantity(double toQuantity) { this.toQuantity = toQuantity; }
        public double getDiscountPercentage() { return discountPercentage; }
        public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    }
}