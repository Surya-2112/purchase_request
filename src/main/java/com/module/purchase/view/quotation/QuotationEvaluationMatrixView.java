package com.module.purchase.view.quotation;

import java.util.List;
import java.util.stream.Collectors;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.service.QuotationService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "quotation-evaluation-matrix", layout = MainLayout.class)
@PermitAll
public class QuotationEvaluationMatrixView extends VerticalLayout implements HasUrlParameter<Long> {

    private final RequestForQuotationService rfqService;
    private final QuotationService quotationService;
    private final SecurityService securityService;

    private RequestForQuotation targetRfq;
    
    private final VerticalLayout quotationCardsStackContainer = new VerticalLayout();
    private final Button backBtn = new Button("Back to Evaluation Center");
    private final Span systemicNoticeMessage = new Span();

    public QuotationEvaluationMatrixView(RequestForQuotationService rfqService, 
                                        QuotationService quotationService,
                                        SecurityService securityService) {
        this.rfqService = rfqService;
        this.quotationService = quotationService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        backBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
        backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("quotation-comparison")));

        systemicNoticeMessage.getStyle()
                .set("font-style", "italic")
                .set("color", "var(--lumo-secondary-text-color)");

        quotationCardsStackContainer.setWidthFull();
        quotationCardsStackContainer.setSpacing(true);
        quotationCardsStackContainer.setPadding(false);

        // Build a Scroller container to handle multiple stacked vendor cards gracefully
        VerticalLayout scrollContent = new VerticalLayout(
            new H2("Request for Quotation - Multi-Bid Stacked Evaluation Worksheet"),
            systemicNoticeMessage,
            new Hr(),
            quotationCardsStackContainer,
            new Hr(),
            backBtn
        );
        scrollContent.setWidthFull();
        scrollContent.setPadding(false);

        Scroller scroller = new Scroller(scrollContent);
        scroller.setSizeFull();
        add(scroller);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        if (id == null) {
            Notification.show("No valid RFQ parameter keys passed.", 3000, Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("quotation-comparison"));
            return;
        }

        rfqService.getRequestForQuotationById(id).ifPresentOrElse(rfq -> {
            this.targetRfq = rfq;
            buildStackedQuotationAnalysisSheets(rfq);
        }, () -> {
            Notification.show("The requested RFQ reference file is missing.", 4000, Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("quotation-comparison"));
        });
    }

    private void buildStackedQuotationAnalysisSheets(RequestForQuotation rfq) {
        quotationCardsStackContainer.removeAll();

        List<Quotation> bidsDataset = quotationService.getQuotationsByRfq(rfq).stream()
                .filter(q -> q.getStatus() != Status.DRAFT && q.getStatus() != Status.CANCELLED)
                .collect(Collectors.toList());

        if (bidsDataset.isEmpty()) {
            systemicNoticeMessage.setText("Notice: No verified vendor pricing models have been logged yet against RFQ-" + rfq.getId());
            return;
        }

        systemicNoticeMessage.setText("💡 Tip: Double-click any line row item within a vendor's pricing grid sheet below to review its cascading multi-tier Slab Volume Discounts matrix.");

        for (Quotation quotation : bidsDataset) {
            quotationCardsStackContainer.add(createStandaloneVendorBidCardBlock(quotation));
        }
    }

    private VerticalLayout createStandaloneVendorBidCardBlock(Quotation quotation) {
        VerticalLayout cardContainer = new VerticalLayout();
        cardContainer.setWidthFull();
        cardContainer.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-sm)");

        String vendorName = quotation.getVendor() != null ? quotation.getVendor().getVendorName() : "Unknown Supplier";
        String docDate = quotation.getQuotationDate() != null ? quotation.getQuotationDate().toString() : "-";
        String grossValue = String.format("%.2f INR", quotation.getTotalAmount());

        Span metaInfoText = new Span(String.format("🏬 Supplier: %s   |   📅 Filed Date: %s   |   💰 Gross Estimate: %s", 
                vendorName, docDate, grossValue));
        metaInfoText.getStyle().set("font-weight", "bold").set("font-size", "15px");

        Button approveBtn = new Button("Approve Vendor Contract", VaadinIcon.CHECK_CIRCLE.create());
        approveBtn.addThemeName("success primary small");
        
        if (quotation.getStatus() == Status.APPROVED) {
            approveBtn.setText("Contract Awarded Winner");
            approveBtn.setEnabled(false);
            cardContainer.getStyle().set("border", "2px solid var(--lumo-success-color)").set("background-color", "#f0fdf4");
        } else if (targetRfq.getStatus() == RequestForQuotationStatus.CLOSED && isAnyBidApprovedInThread()) {
            approveBtn.setVisible(false); 
        }

        approveBtn.addClickListener(e -> executeAwardContractTransaction(quotation));

        HorizontalLayout toolbarHeader = new HorizontalLayout(metaInfoText, approveBtn);
        toolbarHeader.setWidthFull();
        toolbarHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbarHeader.setAlignItems(Alignment.CENTER);

        Grid<QuotationLine> linesGrid = new Grid<>();
        linesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        linesGrid.setAllRowsVisible(true);
        linesGrid.setWidthFull();

        linesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null 
                ? line.getItemVariant().getItem().getItemName() : "").setHeader("Item / Variant Name").setAutoWidth(true);
        
        linesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Sourcing Specification").setAutoWidth(true);

        linesGrid.addColumn(line -> {
            return rfqService.getLinesByRfqId(targetRfq.getId()).stream()
                    .filter(rfqLine -> rfqLine.getItemVariant().getId().equals(line.getItemVariant().getId()))
                    .map(rfqLine -> String.format("%.2f", rfqLine.getRequestedQuantity()))
                    .findFirst().orElse("0.00");
        }).setHeader("Qty Needed").setWidth("110px");

        linesGrid.addColumn(line -> String.format("%.2f INR", line.getUnitPrice())).setHeader("Offered Unit Price").setWidth("140px");

        List<QuotationLine> quotationLines = quotationService.getLinesByQuotation(quotation);
        linesGrid.setItems(quotationLines);

        linesGrid.addItemDoubleClickListener(event -> {
            QuotationLine clickedPricingLine = event.getItem();
            if (clickedPricingLine != null) {
                openTieredSlabsBreakdownModalDialog(clickedPricingLine);
            }
        });

        cardContainer.add(toolbarHeader, new Hr(), linesGrid);
        return cardContainer;
    }

    private void executeAwardContractTransaction(Quotation winnerQuote) {
        try {
            Employee actionBuyerActor = securityService.getLoggedInUser().getEmployee();

            // Process and update evaluation states down the active dataset pipeline
            List<Quotation> currentBidsList = quotationService.getQuotationsByRfq(targetRfq);
            for (Quotation quotation : currentBidsList) {
                if (quotation.getId().equals(winnerQuote.getId())) {
                    quotation.setStatus(Status.APPROVED); 
                } else if (quotation.getStatus() != Status.DRAFT && quotation.getStatus() != Status.CANCELLED) {
                    quotation.setStatus(Status.REJECTED); 
                }
                quotationService.updateQuotation(quotation);
            }

            targetRfq.setStatus(RequestForQuotationStatus.CLOSED);
            rfqService.updateRequestForQuotation(targetRfq, actionBuyerActor);

            Notification.show("Sourcing validation complete. Contract awarded to winner vendor successfully!", 4000, Position.TOP_CENTER);
            
            // FIX: Removed old undefined buildLiveFilteringBars() call. Reroute back to dashboard securely.
            getUI().ifPresent(ui -> ui.navigate("quotation-comparison"));

        } catch (Exception ex) {
            Notification.show("Contract assignment execution matrix failed: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void openTieredSlabsBreakdownModalDialog(QuotationLine line) {
        Dialog slabsModalOverlay = new Dialog();
        slabsModalOverlay.setHeaderTitle("Volume Tier Slab Breakdown");
        slabsModalOverlay.setWidth("600px");

        String itemName = line.getItemVariant() != null ? line.getItemVariant().getItem().getItemName() : "Selected Item";
        VerticalLayout modalLayout = new VerticalLayout(new H3("Tiered discounts mapping rules matching: " + itemName));
        modalLayout.setPadding(false);

        Grid<DiscountType> modalSlabsGrid = new Grid<>();
        modalSlabsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        modalSlabsGrid.setAllRowsVisible(true);
        modalSlabsGrid.setWidthFull();

        modalSlabsGrid.addColumn(DiscountType::getFromQuantity).setHeader("From Quantity").setAutoWidth(true);
        modalSlabsGrid.addColumn(DiscountType::getToQuantity).setHeader("To Quantity").setAutoWidth(true);
        modalSlabsGrid.addColumn(d -> String.format("%.2f %%", d.getDiscountPercentage())).setHeader("Applied Discount %").setAutoWidth(true);

        List<DiscountType> detailedSlabsList = quotationService.getDiscountsByLine(line);
        modalSlabsGrid.setItems(detailedSlabsList);

        if (detailedSlabsList.isEmpty()) {
            modalLayout.add(new Span("⚠️ No specific bulk volume discount slabs were registered for this row item."));
        } else {
            modalLayout.add(modalSlabsGrid);
        }

        Button closeOverlayBtn = new Button("Close Breakdown View", e -> slabsModalOverlay.close());
        closeOverlayBtn.addThemeName("tertiary");
        slabsModalOverlay.getFooter().add(closeOverlayBtn);

        slabsModalOverlay.add(modalLayout);
        slabsModalOverlay.open();
    }

    private boolean isAnyBidApprovedInThread() {
        return quotationService.getQuotationsByRfq(targetRfq).stream()
                .anyMatch(q -> q.getStatus() == Status.APPROVED);
    }
}