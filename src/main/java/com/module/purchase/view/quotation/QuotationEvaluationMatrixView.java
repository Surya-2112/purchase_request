package com.module.purchase.view.quotation;

import java.util.List;
import java.util.stream.Collectors;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.service.QuotationService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
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
    private final AssigningConfigService assigningConfigService;

    private RequestForQuotation targetRfq;

    private final VerticalLayout quotationCardsStackContainer = new VerticalLayout();
    private final Button backBtn = new Button("Back");

    public QuotationEvaluationMatrixView(RequestForQuotationService rfqService, QuotationService quotationService,
                                        SecurityService securityService,  AssigningConfigService assigningConfigService) {
        this.rfqService = rfqService;
        this.quotationService = quotationService;
        this.securityService = securityService;
        this.assigningConfigService = assigningConfigService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        backBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
        backBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("quotation-comparison")));

        quotationCardsStackContainer.setWidthFull();
        quotationCardsStackContainer.setSpacing(true);
        quotationCardsStackContainer.setPadding(false);

        VerticalLayout scrollContent = new VerticalLayout(
                new H2("Received Quotations"),
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
        try{
        rfqService.getRequestForQuotationById(id).ifPresentOrElse(rfq -> {
            this.targetRfq = rfq;
            buildStackedQuotationAnalysisSheets(rfq);
        }, () -> {
            Notification.show("The requested RFQ reference file is missing.", 4000, Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("quotation-comparison"));
        });
        }catch (Exception ex) {
            event.forwardTo("quotation-comparison");
            event.getUI().access(() -> { Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);});
            return;
        }
    }

    private void buildStackedQuotationAnalysisSheets(RequestForQuotation rfq) {
        quotationCardsStackContainer.removeAll();

        List<Quotation> bidsDataset = quotationService.getQuotationsByRfq(rfq).stream()
                .filter(q -> q.getStatus() != Status.DRAFT && q.getStatus() != Status.CANCELLED)
                .collect(Collectors.toList());

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
        String grossValue = String.format("%.2f ", quotation.getTotalAmount());

        double discount=0;
        for(QuotationLine line:quotationService.getLinesByQuotation(quotation))
        { 
            double qty=line.getRequestForQuotationLine().getRequestedQuantity();
            discount+=(qty*((line.getDiscount()*line.getUnitPrice())/100));
        }

        Span metaInfoText = new Span(String.format("Supplier: %s   |   Filed Date: %s   |   Total amount: %s  |\n  Total Discount Amount: %.2f  | Total Amount After Discount: %.2f" ,
                vendorName, docDate, grossValue,discount,quotation.getTotalAmount()-discount));
        metaInfoText.getStyle().set("font-weight", "bold").set("font-size", "15px").set("white-space", "pre-line");

        Button approveBtn = new Button("Approve Vendor Quotations");
        approveBtn.addThemeName("success primary small");

        if (quotation.getStatus() == Status.APPROVED) {
            approveBtn.setText("Quotations Allocated");
            approveBtn.setEnabled(false);
            cardContainer.getStyle().set("border", "2px solid var(--lumo-success-color)").set("background-color", "#f0fdf4");
        } else if (quotation.getStatus() == Status.WAITING_APPROVAL) { 
            
            List<Quotation> currentBidsList = quotationService.getQuotationsByRfq(targetRfq).stream()
                    .filter(q -> q.getStatus() != Status.DRAFT && q.getStatus() != Status.CANCELLED)
                    .collect(Collectors.toList());
            
            double lowestBidAmount = currentBidsList.stream().mapToDouble(Quotation::getTotalAmount).min().orElse(0.0);
            
            List<AssigningConfig> structuralTiersChain = assigningConfigService
                    .determineRequiredApprovals(quotation.getTotalAmount(), lowestBidAmount, ApprovalType.QUOTATION);

            EmployeeGroup currentActiveGroupRequirement = structuralTiersChain.stream()
                    .map(AssigningConfig::getEmployeeGroup)
                    .findFirst() 
                    .orElse(null);

            List<EmployeeGroup> currentUserGroups = securityService.getLoggedInUser().getEmployee().getRole().getEmployeeGroups();

            boolean hasAuthority = currentUserGroups != null && 
                    (currentUserGroups.contains(currentActiveGroupRequirement) || currentUserGroups.contains(EmployeeGroup.SUPER_ADMIN));

            if (hasAuthority) {
                approveBtn.setText("Approve Vendor Quotations");
                approveBtn.addThemeName("primary warning");
                approveBtn.setEnabled(true);
                cardContainer.getStyle().set("border", "2px solid var(--lumo-warning-color)").set("background-color", "#fffbeb");
            } else {
                String targetGroupName = currentActiveGroupRequirement != null ? currentActiveGroupRequirement.name() : "Approver";
                approveBtn.setText("Awaiting " + targetGroupName + " Review");
                approveBtn.setEnabled(false);
                approveBtn.addThemeName("contrast");
                cardContainer.getStyle().set("border", "2px solid var(--lumo-contrast-30pct)").set("background-color", "var(--lumo-contrast-5pct)");
            }

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

        linesGrid.addColumn(line -> String.format("%.2f", line.getUnitPrice())).setHeader("Offered Unit Price").setWidth("140px");
        linesGrid.addColumn(line-> line.getDiscount()==null?0.0:line.getDiscount()).setHeader("Offered Discount Percentage").setWidth("140px");

        List<QuotationLine> quotationLines = quotationService.getLinesByQuotation(quotation);
        linesGrid.setItems(quotationLines);

        cardContainer.add(toolbarHeader, new Hr(), linesGrid);
        return cardContainer;
    }

    private void executeAwardContractTransaction(Quotation winnerQuote) {
        try {
            Employee actionBuyerActor = securityService.getLoggedInUser().getEmployee();
            List<Quotation> currentBidsList = quotationService.getQuotationsByRfq(targetRfq).stream()
                    .filter(q -> q.getStatus() != Status.DRAFT && q.getStatus() != Status.CANCELLED)
                    .collect(Collectors.toList());

            double lowestBidAmount = currentBidsList.stream().mapToDouble(Quotation::getTotalAmount).min().orElse(0.0);
            double chosenBidAmount = winnerQuote.getTotalAmount();

            List<AssigningConfig> requiredApprovalTiers = assigningConfigService
                    .determineRequiredApprovals(chosenBidAmount, lowestBidAmount, ApprovalType.QUOTATION);

            if (winnerQuote.getStatus() == Status.WAITING_APPROVAL) {
                List<EmployeeGroup> currentUserGroups = actionBuyerActor.getRole().getEmployeeGroups();
                
                boolean isHighestTierSatisfied = requiredApprovalTiers.isEmpty() || currentUserGroups.contains(EmployeeGroup.SUPER_ADMIN) ||
                        currentUserGroups.contains(requiredApprovalTiers.get(requiredApprovalTiers.size() - 1).getEmployeeGroup());

                if (isHighestTierSatisfied) {
                    for (Quotation quotation : currentBidsList) {
                        if (quotation.getId().equals(winnerQuote.getId())) {
                            quotation.setStatus(Status.APPROVED);
                        } else {
                            quotation.setStatus(Status.REJECTED);
                        }
                        quotationService.updateQuotation(quotation);
                    }
                    targetRfq.setStatus(RequestForQuotationStatus.CLOSED);
                    rfqService.updateRequestForQuotation(targetRfq, actionBuyerActor);

                    Notification.show("Final authorization secured. Vendor contract awarded successfully!", 4000, Position.TOP_CENTER);
                } else {
                    Notification.show("Level sign-off verified. Document advanced to next workflow authority tier.", 4000, Position.TOP_CENTER);
                }
                
            } else {
                if (requiredApprovalTiers.isEmpty()) {
                    for (Quotation quotation : currentBidsList) {
                        if (quotation.getId().equals(winnerQuote.getId())) {
                            quotation.setStatus(Status.APPROVED);
                        } else {
                            quotation.setStatus(Status.REJECTED);
                        }
                        quotationService.updateQuotation(quotation);
                    }
                    targetRfq.setStatus(RequestForQuotationStatus.CLOSED);
                    rfqService.updateRequestForQuotation(targetRfq, actionBuyerActor);

                    Notification.show("Contract awarded automatically! No external manager approvals were required.", 4000, Position.TOP_CENTER);
                } else {
                    String levelsChainSummary = requiredApprovalTiers.stream()
                            .map(tier -> "Level " + tier.getLevel() + " (" + tier.getEmployeeGroup().name() + ")")
                            .collect(Collectors.joining(" ➔ "));

                    winnerQuote.setStatus(Status.WAITING_APPROVAL);
                    quotationService.updateQuotation(winnerQuote);

                    Notification.show("Routing Request! This award requires authorization chain: " + levelsChainSummary, 6000, Position.TOP_CENTER);
                }
            }

            getUI().ifPresent(ui -> ui.navigate("quotation-comparison"));

        } catch (Exception ex) {
            Notification.show("Workflow engine failed to evaluate assignment criteria: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }
    private boolean isAnyBidApprovedInThread() {
        return quotationService.getQuotationsByRfq(targetRfq).stream()
                .anyMatch(q -> q.getStatus() == Status.APPROVED);
    }
}