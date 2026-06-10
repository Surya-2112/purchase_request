package com.module.purchase.view.quotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
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

    private final RequestForQuotationService rfqService;
    private final QuotationService quotationService;
    private final VendorService vendorService;
    private final SecurityService securityService;

    private RequestForQuotation targetRfq;
    private Vendor selectedVendorContext;
    private boolean isVendorUser = false;
    private QuotationLine activeSelectedLine;

    // Header Components
    private final TextField rfqIdField = new TextField("Target RFQ Reference");
    private final ComboBox<Vendor> vendorSelector = new ComboBox<>("Select Supplying Vendor *");
    private final TextField quotationDateField = new TextField("Quotation Date");
    private final NumberField totalAmountField = new NumberField("Calculated Gross Total Amount");

    // Live Master Bidding Grid (Using core Entity directly!)
    private final Grid<QuotationLine> biddingGrid = new Grid<>();
    private final List<QuotationLine> biddingDataset = new ArrayList<>();

    // Map to link temporary Discount list arrays to each QuotationLine in memory
    private final Map<QuotationLine, List<DiscountType>> lineDiscountsMap = new HashMap<>();

    // Slab Components
    private final VerticalLayout discountSubPanel = new VerticalLayout();
    private final Span discountPanelHeader = new Span("Select an item above to map tiered volume break discounts.");
    private final Grid<DiscountType> discountMatrixGrid = new Grid<>();
    private final Button addSlabRowBtn = new Button("Add Discount Slab Tier Row");

    // Actions
    private final Button saveDraftBtn = new Button("Save as Draft");
    private final Button submitBidBtn = new Button("Submit Final Bid");
    private final Button cancelBtn = new Button("Cancel");

    public QuotationFormView(RequestForQuotationService rfqService,
            QuotationService quotationService,
            VendorService vendorService,
            SecurityService securityService) {
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
                    lineDiscountsMap.get(activeSelectedLine).remove(d);
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

        // 3. ACTION BUTTONS
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

    private void populateDiscountSubPanel(QuotationLine line) {
        this.activeSelectedLine = line;
        discountPanelHeader.setText("Configuring tiered discount matrix parameters matching row variant: " +
                (line.getItemVariant() != null ? line.getItemVariant().getItem().getItemName() : ""));
        discountMatrixGrid.setItems(lineDiscountsMap.get(line));
        discountSubPanel.setVisible(true);
    }

    private void clearDiscountSubPanel() {
        this.activeSelectedLine = null;
        discountSubPanel.setVisible(false);
    }

    private void addBlankDiscountSlabRowEntry() {
        if (activeSelectedLine != null) {
            lineDiscountsMap.get(activeSelectedLine).add(new DiscountType());
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

                List<RequestForQuotationLine> requiredLines = rfqService.getLinesByRfqId(rfq.getId());

                // FIX: Look up vendor list via the single, explicit category of this RFQ
                if (!isVendorUser && !requiredLines.isEmpty()) {
                    Category rfqCategory = requiredLines.get(0).getItemVariant().getItem().getCategory();

                    vendorSelector.setItems(vendorService.getVendorsByCategory(rfqCategory));
                } else if (isVendorUser) {
                    checkSubmissionComplianceGuard();
                }

                // Hydrate bidding sheet using target QuotationLine objects directly
                biddingDataset.clear();
                lineDiscountsMap.clear();

                for (RequestForQuotationLine rfqLine : requiredLines) {
                    QuotationLine pricingRow = new QuotationLine();
                    pricingRow.setItemVariant(rfqLine.getItemVariant());
                    pricingRow.setUnitPrice(0.0);

                    biddingDataset.add(pricingRow);
                    lineDiscountsMap.put(pricingRow, new ArrayList<>()); // Maps clean nested discount collection
                                                                         // indices
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
            return;
        }

        // Fetch the active target source requirements once to prevent repeating
        // overhead lookups
        List<RequestForQuotationLine> rfqLines = rfqService.getLinesByRfqId(targetRfq.getId());
        double currentRunningSum = 0.0;

        for (QuotationLine pricingLine : biddingDataset) {
            if (pricingLine.getItemVariant() != null) {
                double qty = rfqLines.stream()
                        .filter(rfqLine -> rfqLine.getItemVariant() != null &&
                                rfqLine.getItemVariant().getId().equals(pricingLine.getItemVariant().getId()))
                        .map(RequestForQuotationLine::getRequestedQuantity)
                        .findFirst()
                        .orElse(0.0);

                double price = pricingLine.getUnitPrice() != null ? pricingLine.getUnitPrice() : 0.0;
                currentRunningSum += (qty * price);
            }
        }
        totalAmountField.setValue(currentRunningSum);
    }

    private void processTransactionCommit(Status targetedLifecycleState) {
        if (selectedVendorContext == null) {
            Notification.show("Validation Block: Please specify the supplying vendor owner.", 4000, Position.MIDDLE);
            return;
        }

        if (quotationService.isDuplicateSubmission(targetRfq.getId(), selectedVendorContext.getVendorId())) {
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

            for (DiscountType discount : lineDiscountsMap.get(line)) {
                if (discount.getFromQuantity() >= discount.getToQuantity()) {
                    Notification.show(
                            "Validation Fault: Volume slab start boundary must sit strictly below end limits.", 4000,
                            Position.MIDDLE);
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

            masterProposal = quotationService.addQuotation(masterProposal);

            // 2. Persist downstream child proposal item details lines
            for (QuotationLine pricingLine : biddingDataset) {
                pricingLine.setQuotation(masterProposal);
                QuotationLine savedLine = quotationService.saveQuotationLine(pricingLine);

                // 3. Persist nested cascading multi-tier volume break slab discounts mappings
                // logs
                List<DiscountType> discounts = lineDiscountsMap.get(pricingLine);
                for (DiscountType slab : discounts) {
                    slab.setQuotationLine(savedLine);
                    quotationService.saveDiscountType(slab);
                }
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
        if (isVendorUser) {
            getUI().ifPresent(ui -> ui.navigate("vendor-sourcing"));
        } else {
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation"));
        }
    }
}