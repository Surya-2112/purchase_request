package com.module.purchase.view.quotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    private QuotationLine activeSelectedLine; 

    // Master Header Form Layout Mappings
    private final TextField rfqIdField = new TextField("Target RFQ Reference");
    private final ComboBox<Vendor> vendorSelector = new ComboBox<>("Select Supplying Vendor *");
    private final TextField quotationDateField = new TextField("Quotation Date");
    private final NumberField totalAmountField = new NumberField("Calculated Gross Total Amount");

    // Live Master Bidding Worksheet Grid Layout
    private final Grid<QuotationLine> biddingGrid = new Grid<>();
    private final List<QuotationLine> biddingDataset = new ArrayList<>();
    
    // Auxiliary tracking container to safely preserve requested volumes for calculations
    private final List<Double> requestedQuantitiesTracker = new ArrayList<>();

    // Live Detail Slab Discount Pricing Matrix Layout components
    private final VerticalLayout discountSubPanel = new VerticalLayout();
    private final Span discountPanelHeader = new Span("Select an item above to map tiered volume break discounts.");
    private final Grid<DiscountType> discountMatrixGrid = new Grid<>();
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
        biddingGrid.addColumn(ql -> ql.getItemVariant() != null && ql.getItemVariant().getItem() != null
                ? ql.getItemVariant().getItem().getItemName() : "").setHeader("Item Name").setAutoWidth(true);
        
        biddingGrid.addColumn(ql -> ql.getItemVariant() != null ? ql.getItemVariant().getSpecification() : "")
                .setHeader("Specification Requirement").setAutoWidth(true);
        
        // FIXED: Retrieve row sequence quantities using list indices safely
        biddingGrid.addColumn(ql -> {
            int index = biddingDataset.indexOf(ql);
            return (index >= 0 && index < requestedQuantitiesTracker.size()) ? requestedQuantitiesTracker.get(index) : 0;
        }).setHeader("Quantity Needed").setWidth("140px");

        biddingGrid.addComponentColumn(ql -> {
            NumberField unitPriceInput = new NumberField();
            Double currentPrice = ql.getUnitPrice();
            unitPriceInput.setValue(currentPrice != null ? currentPrice : 0.0);
            unitPriceInput.setMin(0.01);
            unitPriceInput.setPlaceholder("Enter Base Price...");
            unitPriceInput.setWidth("160px");
            unitPriceInput.addValueChangeListener(change -> {
                ql.setUnitPrice(change.getValue() != null ? change.getValue() : 0.0);
                recalculateGrossTotalAmount(); 
            });
            return unitPriceInput;
        }).setHeader("Unit Price *").setWidth("180px");

        biddingGrid.setAllRowsVisible(true);
        biddingGrid.setWidthFull();
        biddingGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

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
            Double fromQty = d.getFromQuantity();
            fromQtyInput.setValue(fromQty != null ? fromQty : 0.0);
            fromQtyInput.setMin(0.0);
            fromQtyInput.setPlaceholder("Min Qty...");
            fromQtyInput.addValueChangeListener(c -> d.setFromQuantity(c.getValue() != null ? c.getValue() : 0.0));
            return fromQtyInput;
        }).setHeader("From Quantity").setWidth("160px");

        discountMatrixGrid.addComponentColumn(d -> {
            NumberField toQtyInput = new NumberField();
            Double toQty = d.getToQuantity();
            toQtyInput.setValue(toQty != null ? toQty : 0.0);
            toQtyInput.setMin(0.1);
            toQtyInput.setPlaceholder("Max Qty...");
            toQtyInput.addValueChangeListener(c -> d.setToQuantity(c.getValue() != null ? c.getValue() : 0.0));
            return toQtyInput;
        }).setHeader("To Quantity").setWidth("160px");

        discountMatrixGrid.addComponentColumn(d -> {
            NumberField pctInput = new NumberField();
            Double pct = d.getDiscountPercentage();
            pctInput.setValue(pct != null ? pct : 0.0);
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
                if (activeSelectedLine != null && activeSelectedLine.getDiscountTypes() != null) {
                    activeSelectedLine.getDiscountTypes().remove(d);
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
        discountSubPanel.setVisible(false);

        // 3. ACTION COMMITS
        saveDraftBtn.addThemeName("secondary contrast");
        saveDraftBtn.addClickListener(e -> processTransactionCommit(Status.DRAFT));

        submitBidBtn.addThemeName("primary success");
        submitBidBtn.setIcon(VaadinIcon.PAPERPLANE.create());
        submitBidBtn.addClickListener(e -> processTransactionCommit(Status.WAITING_APPROVAL));

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

    private void populateDiscountSubPanel(QuotationLine line) {
        this.activeSelectedLine = line;
        // FIXED: Initializing with HashSet to accommodate your entity signature property structure
        if (line.getDiscountTypes() == null) {
            line.setDiscountTypes(new HashSet<>());
        }
        
        discountPanelHeader.setText("Configuring tiered discount matrix parameters matching row variant: " + 
                (line.getItemVariant() != null ? line.getItemVariant().getItem().getItemName() : ""));
        discountMatrixGrid.setItems(line.getDiscountTypes());
        discountSubPanel.setVisible(true);
    }

    private void clearDiscountSubPanel() {
        this.activeSelectedLine = null;
        discountSubPanel.setVisible(false);
    }

    private void addBlankDiscountSlabRowEntry() {
        if (activeSelectedLine != null) {
            if (activeSelectedLine.getDiscountTypes() == null) {
                activeSelectedLine.setDiscountTypes(new HashSet<>());
            }
            DiscountType emptySlab = new DiscountType();
            emptySlab.setQuotationLine(activeSelectedLine); 
            activeSelectedLine.getDiscountTypes().add(emptySlab);
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
                requestedQuantitiesTracker.clear();
                
                List<RequestForQuotationLine> requiredLines = rfqService.getLinesByRfqId(rfq.getId());
                for (RequestForQuotationLine rfqLine : requiredLines) {
                    QuotationLine line = new QuotationLine();
                    line.setItemVariant(rfqLine.getItemVariant());
                    line.setUnitPrice(0.0);
                    line.setDiscountTypes(new HashSet<>()); // FIXED
                    
                    biddingDataset.add(line);
                    // Tracking quantities out-of-band to decouple from entity schemas safely
                    Double reqQty = rfqLine.getRequestedQuantity();
                    requestedQuantitiesTracker.add(reqQty != null ? reqQty : 0);
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
        for (int i = 0; i < biddingDataset.size(); i++) {
            QuotationLine line = biddingDataset.get(i);
            double qty = (i < requestedQuantitiesTracker.size()) ? requestedQuantitiesTracker.get(i) : 0.0;
            Double unitPrice = line.getUnitPrice();
            currentRunningSum += (qty * (unitPrice != null ? unitPrice : 0.0));
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
        for (QuotationLine ql : biddingDataset) {
            Double price = ql.getUnitPrice();
            if (price == null || price <= 0) {
                Notification.show("Validation Fault: Every requested item variant line requires a valid, positive base price.", 4000, Position.MIDDLE);
                return;
            }
            if (ql.getDiscountTypes() != null) {
                for (DiscountType d : ql.getDiscountTypes()) {
                    Double fromQty = d.getFromQuantity();
                    Double toQty = d.getToQuantity();
                    if (fromQty != null && toQty != null && fromQty >= toQty) {
                        Notification.show("Validation Fault: Volume slab start boundary must sit strictly below end limits.", 4000, Position.MIDDLE);
                        return;
                    }
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
            for (QuotationLine line : biddingDataset) {
                line.setQuotation(masterProposal);
                QuotationLine savedLine = quotationService.saveQuotationLine(line);
                
                if (line.getDiscountTypes() != null) {
                    for (DiscountType slab : line.getDiscountTypes()) {
                        slab.setQuotationLine(savedLine);
                        quotationService.saveDiscountType(slab);
                    }
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
}