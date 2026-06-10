package com.module.purchase.view.quotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.entityDTO.QuotationDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.service.QuotationService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "quotation-comparison", layout = MainLayout.class)
@PermitAll
public class QuotationComparisonView extends VerticalLayout {

    private final RequestForQuotationService rfqService;
    private final QuotationService quotationService;

    private final ComboBox<RequestForQuotation> rfqSelector = new ComboBox<>("Select RFQ to Compare Bids");
    private final HorizontalLayout summaryCardsLayout = new HorizontalLayout();
    private final Grid<ComparisonRow> comparisonGrid = new Grid<>();
    
    private List<Quotation> activeQuotationsForRfq = new ArrayList<>();

    public QuotationComparisonView(RequestForQuotationService rfqService, QuotationService quotationService) {
        this.rfqService = rfqService;
        this.quotationService = quotationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        setupHeaderSelector();
        setupComparisonGridBaseStructure();

        add(
            new H2("Quotations Evaluation & Comparison Matrix"),
            rfqSelector,
            new H3("Gross Bid Summaries Overview"),
            summaryCardsLayout,
            new Hr(),
            new H3("Detailed Item-by-Item Price Matrix Breakdown"),
            comparisonGrid
        );
        expand(comparisonGrid);
    }

    private void setupHeaderSelector() {
        rfqSelector.setItems(rfqService.getAllRequestsForQuotation()); 
        // FIXED: Removed undefined getDepartment() dependency reference hook
        rfqSelector.setItemLabelGenerator(rfq -> "RFQ REFERENCE #" + rfq.getId());
        rfqSelector.setPlaceholder("Choose an RFQ tracking thread...");
        rfqSelector.setWidth("350px");
        rfqSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                executeComparisonMatrixPipeline(e.getValue());
            } else {
                clearComparisonMatrixView();
            }
        });
    }

    private void setupComparisonGridBaseStructure() {
        comparisonGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        comparisonGrid.setWidthFull();
        
        comparisonGrid.addColumn(ComparisonRow::getItemName).setHeader("Material / Variant Name").setAutoWidth(true).setFrozen(true);
        comparisonGrid.addColumn(ComparisonRow::getSpecification).setHeader("Sourcing Specification").setAutoWidth(true);
        
        // Formatted cleanly to support fractional quantities representation maps
        comparisonGrid.addColumn(row -> String.format("%.2f", row.getRequestedQuantity())).setHeader("Qty Needed").setWidth("120px").setFlexGrow(0);
    }

    private void executeComparisonMatrixPipeline(RequestForQuotation rfq) {
        clearComparisonMatrixView();

        // FIXED: Fetch quotes using your standard query parameter engine strategies
        QuotationDTO criteria = new QuotationDTO();
        criteria.setRequestForQuotation(rfq);
        
        // Fetch a large page breakdown chunk to guarantee matrix mapping hits
        activeQuotationsForRfq = quotationService.getAllQuotations(criteria, 0, 500).getContent().stream()
                .map(dto -> {
                    // Reconstruct thin wrapper entities safely for rendering layers
                    Quotation q = new Quotation();
                    q.setId(dto.getId());
                    q.setVendor(dto.getVendor());
                    q.setTotalAmount(dto.getTotalAmount());
                    q.setStatus(dto.getStatus());
                    q.setRequestForQuotation(dto.getRequestForQuotation());
                    q.setQuotationDate(dto.getQuotationDate());
                    return q;
                })
                .filter(q -> q.getStatus() != Status.DRAFT)
                .collect(Collectors.toList());

        if (activeQuotationsForRfq.isEmpty()) {
            summaryCardsLayout.add(new Span("No final bids have been submitted yet for this RFQ reference."));
            return;
        }

        double lowestTotal = activeQuotationsForRfq.stream()
                .mapToDouble(q -> q.getTotalAmount() != null ? q.getTotalAmount() : 0.0)
                .min().orElse(0.0);

        for (Quotation quote : activeQuotationsForRfq) {
            boolean isLowest = (quote.getTotalAmount() != null) && (quote.getTotalAmount() == lowestTotal);
            summaryCardsLayout.add(createVendorSummaryCard(quote, isLowest));
        }

        List<RequestForQuotationLine> rfqLines = rfqService.getLinesByRfqId(rfq.getId());
        List<ComparisonRow> rows = new ArrayList<>();

        for (RequestForQuotationLine rfqLine : rfqLines) {
            // FIXED: Evaluates numeric bounds safely using Double type signatures
            Double qty = rfqLine.getRequestedQuantity();
            ComparisonRow row = new ComparisonRow(
                rfqLine.getItemVariant().getItem().getItemName(),
                rfqLine.getItemVariant().getSpecification(),
                qty != null ? qty : 0.0
            );

            // Cross-reference what price each vendor quoted for this exact variant item segment
            for (Quotation quote : activeQuotationsForRfq) {
                // FIXED: Pull lines safely from database using fallback entity collections parameters
                Quotation persistentQuote = quotationService.getQuotationById(quote.getId()).orElse(null);
                if (persistentQuote != null && persistentQuote.getQuotationLines() != null) {
                    for (QuotationLine ql : persistentQuote.getQuotationLines()) {
                        if (ql.getItemVariant().getId().equals(rfqLine.getItemVariant().getId())) {
                            row.addVendorPrice(quote.getVendor().getVendorId(), ql.getUnitPrice());
                            row.addVendorSlabs(quote.getVendor().getVendorId(), ql.getDiscountTypes());
                        }
                    }
                }
            }
            rows.add(row);
        }

        // Dynamically append horizontal price layout matrices lanes
        for (Quotation quote : activeQuotationsForRfq) {
            Long vendorId = quote.getVendor().getVendorId();
            String columnName = quote.getVendor().getVendorName();

            comparisonGrid.addComponentColumn(row -> {
                Double unitPrice = row.getPriceForVendor(vendorId);
                Set<DiscountType> slabs = row.getSlabsForVendor(vendorId);
                
                VerticalLayout cellLayout = new VerticalLayout();
                cellLayout.setPadding(false);
                cellLayout.setSpacing(false);

                if (unitPrice != null) {
                    Span priceSpan = new Span(String.format("%.2f INR", unitPrice));
                    priceSpan.getStyle().set("font-weight", "bold");
                    cellLayout.add(priceSpan);

                    if (slabs != null && !slabs.isEmpty()) {
                        Span slabBadge = new Span(slabs.size() + " Tiered Slabs");
                        slabBadge.getElement().getThemeList().add("badge success small");
                        slabBadge.getStyle().set("font-size", "10px");
                        cellLayout.add(slabBadge);
                    }
                } else {
                    cellLayout.add(new Span("-"));
                }
                return cellLayout;
            }).setHeader(columnName).setAutoWidth(true);
        }

        comparisonGrid.setItems(rows);
    }

    private Div createVendorSummaryCard(Quotation quote, boolean isLowestBidder) {
        Div card = new Div();
        card.getStyle()
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "8px")
            .set("padding", "16px")
            .set("background-color", isLowestBidder ? "#f0fdf4" : "var(--lumo-base-color)")
            .set("box-shadow", "var(--lumo-box-shadow-xs)")
            .set("min-width", "220px");

        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setPadding(false);
        cardContent.setSpacing(false);

        Span name = new Span(quote.getVendor() != null ? quote.getVendor().getVendorName() : "Unknown Vendor");
        name.getStyle().set("font-weight", "bold").set("font-size", "16px");

        Double totalAmt = quote.getTotalAmount();
        Span total = new Span(String.format("%.2f INR", totalAmt != null ? totalAmt : 0.0));
        total.getStyle().set("font-size", "20px").set("font-weight", "bold");
        if (isLowestBidder) {
            total.getStyle().set("color", "#15803d");
        }

        cardContent.add(name, total);

        if (isLowestBidder) {
            Span bestBadge = new Span(VaadinIcon.TROPHY.create());
            bestBadge.add(new Span(" Lowest Offer"));
            bestBadge.getElement().getThemeList().add("badge success primary");
            cardContent.add(bestBadge);
        }

        card.add(cardContent);
        return card;
    }

    private void clearComparisonMatrixView() {
        summaryCardsLayout.removeAll();
        comparisonGrid.getColumns().stream()
                .filter(col -> col.getHeaderText() != null && !col.getHeaderText().equals("Material / Variant Name") 
                        && !col.getHeaderText().equals("Sourcing Specification") && !col.getHeaderText().equals("Qty Needed"))
                .forEach(comparisonGrid::removeColumn);
        comparisonGrid.setItems(new ArrayList<>());
    }

    // ================= CUSTOM MATRIX TRANSFORMATION POJO ROUTINE =================
    public static class ComparisonRow {
        private final String itemName;
        private final String specification;
        private final Double requestedQuantity; // FIXED: Double representation mapping matching entities
        private final Map<Long, Double> vendorPrices = new java.util.HashMap<>();
        private final Map<Long, Set<DiscountType>> vendorSlabs = new java.util.HashMap<>();

        public ComparisonRow(String itemName, String specification, Double requestedQuantity) {
            this.itemName = itemName;
            this.specification = specification;
            this.requestedQuantity = requestedQuantity;
        }

        public String getItemName() { return itemName; }
        public String getSpecification() { return specification; }
        public Double getRequestedQuantity() { return requestedQuantity; }

        public void addVendorPrice(Long vendorId, Double price) { vendorPrices.put(vendorId, price); }
        public Double getPriceForVendor(Long vendorId) { return vendorPrices.get(vendorId); }

        public void addVendorSlabs(Long vendorId, Set<DiscountType> slabs) { vendorSlabs.put(vendorId, slabs); }
        public Set<DiscountType> getSlabsForVendor(Long vendorId) { return vendorSlabs.get(vendorId); }
    }
}