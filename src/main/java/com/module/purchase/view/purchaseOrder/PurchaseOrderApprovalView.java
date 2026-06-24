package com.module.purchase.view.purchaseOrder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.enums.ApprovalSource;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.service.PurchaseOrderHeaderService;
import com.module.purchase.service.PurchaseOrderLineService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-order-approval/:id", layout = com.module.purchase.view.MainLayout.class)
@PermitAll
public class PurchaseOrderApprovalView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseOrderHeaderService poHeaderService;
    private final PurchaseOrderLineService poLineService;
    private final AssigningConfigService configService;
    private final AssigningApprovalsService assigningApprovalsService;
    private final SecurityService securityService;

    private PurchaseOrderHeader poHeader;

    private final List<AssigningApprovals> approvalTiersDataset = new ArrayList<>();
    private final Grid<AssigningApprovals> workflowGrid = new Grid<>(AssigningApprovals.class, false);
    private final Grid<PurchaseOrderLine> itemsGrid = new Grid<>(PurchaseOrderLine.class, false);

    private final Span totalPoAmountText = new Span();

    private final Button addApproverBtn = new Button("Add Manual Approver", VaadinIcon.PLUS.create());
    private final Button submitPoBtn = new Button("Submit Purchase Order", VaadinIcon.PAPERPLANE.create());

    public PurchaseOrderApprovalView(
            PurchaseOrderHeaderService poHeaderService,
            PurchaseOrderLineService poLineService,
            AssigningConfigService configService,
            AssigningApprovalsService assigningApprovalsService,
            SecurityService securityService) {

        this.poHeaderService = poHeaderService;
        this.poLineService = poLineService;
        this.configService = configService;
        this.assigningApprovalsService = assigningApprovalsService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        submitPoBtn.addThemeName("primary success");
        addApproverBtn.addThemeName("outline primary small");

        addApproverBtn.addClickListener(e -> insertManualApprovalTierLine());
        submitPoBtn.addClickListener(e -> executeCommitAndSubmitWorkflow());

        configureItemsGrid();
        configureWorkflowGrid();

        VerticalLayout coreFormContainer = new VerticalLayout(
                new H2("Purchase Order and Add Approvers"),
                totalPoAmountText,
                new Hr(),
                new H3("Purchase Order Lines"),
                itemsGrid,
                new Hr(),
                new H3("Add Approvers"),
                new HorizontalLayout(addApproverBtn, submitPoBtn),
                workflowGrid);
        coreFormContainer.setWidthFull();
        coreFormContainer.setPadding(false);

        Scroller scrollerContainer = new Scroller(coreFormContainer);
        scrollerContainer.setSizeFull();
        add(scrollerContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            Long poId = Long.valueOf(event.getRouteParameters().get("id").orElseThrow());
            poHeaderService.getPurchaseOrderHeaderById(poId).ifPresentOrElse(po -> {
                this.poHeader = po;
                refreshFormMetricsAndDataset();
                loadAutomaticMatrixRules();
            }, () -> {
                throw new RuntimeException("Target Purchase Order not found.");
            });
        } catch (NumberFormatException e) {
            event.forwardTo(ViewName.PURCHASE_ORDER.getRoute());
            event.getUI().access(() -> {
                Notification.show("url is not valid ," + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
            });
            return;
        } catch (Exception ex) {
            event.forwardTo(ViewName.PURCHASE_ORDER.getRoute());
            event.getUI().access(() -> {
                Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
            });
            return;
        }
    }

    private void configureItemsGrid() {
        itemsGrid.removeAllColumns();
        itemsGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName()
                : "").setHeader("Item Name").setAutoWidth(true);

        itemsGrid.addColumn(PurchaseOrderLine::getQuantity).setHeader("Quantity").setWidth("120px");
        itemsGrid.addColumn(line -> String.format("%.2f ", line.getUnitPrice())).setHeader("Unit Price")
                .setWidth("140px");
        itemsGrid.addColumn(line -> String.format("%.2f ", line.getDiscountAmount())).setHeader("Slab Discount")
                .setWidth("140px");
        itemsGrid.addColumn(line -> String.format("%.2f ", line.getTotalAmount())).setHeader("Total Amount")
                .setWidth("150px");

        itemsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        itemsGrid.setAllRowsVisible(true);
    }

    private void configureWorkflowGrid() {
        workflowGrid.removeAllColumns();
        workflowGrid.addColumn(AssigningApprovals::getLevel).setHeader("Level ").setWidth("100px").setFlexGrow(0);

        workflowGrid.addComponentColumn(item -> {
            ComboBox<EmployeeGroup> groupCombo = new ComboBox<>();
            groupCombo.setItemLabelGenerator(EmployeeGroup::getDisplayName);

            groupCombo.addFocusListener(event -> {
                Set<EmployeeGroup> activeAllocations = approvalTiersDataset.stream()
                        .map(AssigningApprovals::getEmployeeGroup)
                        .filter(Objects::nonNull)
                        .filter(g -> !g.equals(item.getEmployeeGroup()))
                        .collect(Collectors.toSet());

                List<EmployeeGroup> legalOptions = EmployeeGroup.getApprovalGroups().stream()
                        .filter(g -> !activeAllocations.contains(g))
                        .toList();

                groupCombo.setItems(legalOptions);
            });

            groupCombo.setItems(EmployeeGroup.getApprovalGroups());

            if (item.getSource() == ApprovalSource.AUTO) {
                List<AssigningConfig> systemConfigs = configService.getConfigs(
                        ApprovalType.PURCHASE_ORDER,
                        poHeader != null ? poHeader.getTotalAmount() : 0.0);

                AssigningConfig validMatch = systemConfigs.stream()
                        .filter(c -> c.getLevel() != null && Objects.equals(c.getLevel(), item.getLevel()))
                        .findFirst().orElse(null);

                if (validMatch != null) {
                    groupCombo.setValue(validMatch.getEmployeeGroup());
                    item.setEmployeeGroup(validMatch.getEmployeeGroup());
                }
            } else {
                if (item.getEmployeeGroup() != null) {
                    groupCombo.setValue(item.getEmployeeGroup());
                }
            }

            groupCombo.addValueChangeListener(e -> {
                item.setEmployeeGroup(e.getValue());
                workflowGrid.getDataProvider().refreshAll();
            });

            groupCombo.setReadOnly(item.getSource() == ApprovalSource.AUTO);
            return groupCombo;
        }).setHeader("Assigned Role Group").setAutoWidth(true);

        workflowGrid.addColumn(a -> a.getSource() != null ? a.getSource().name() : "").setHeader("Source")
                .setWidth("140px");

        workflowGrid.addComponentColumn(item -> {
            Button removeStepBtn = new Button("Remove");
            removeStepBtn.addClickListener(e -> {
                approvalTiersDataset.remove(item);
                resequenceWorkflowTiers();
                workflowGrid.setItems(approvalTiersDataset);
            });

            if (item.getSource() == ApprovalSource.AUTO) {
                removeStepBtn.setVisible(false);
            }
            return removeStepBtn;
        }).setHeader("Actions").setWidth("140px");

        workflowGrid.setItems(approvalTiersDataset);
        workflowGrid.setAllRowsVisible(true);
    }

    private void refreshFormMetricsAndDataset() {
        PurchaseOrderLine poline = new PurchaseOrderLine();
        poline.setPurchaseOrderHeader(poHeader);
        List<PurchaseOrderLine> poLines = poLineService.getPurchaseOrderList(poline);
        itemsGrid.setItems(poLines);

        double totalPoValue = poHeader.getTotalAmount() != null ? poHeader.getTotalAmount() : 0.0;
        totalPoAmountText.setText("Purchase Order Total Amount: " + String.format("%.2f ", totalPoValue));
        totalPoAmountText.getStyle().set("font-weight", "bold").set("font-size", "18px");
    }

    private void loadAutomaticMatrixRules() {
        approvalTiersDataset.clear();

        List<AssigningConfig> automatedRules = configService.getConfigs(
                ApprovalType.PURCHASE_ORDER,
                poHeader.getTotalAmount());

        automatedRules.sort(Comparator.comparing(AssigningConfig::getLevel));

        for (AssigningConfig configurationStep : automatedRules) {
            AssigningApprovals taskRow = new AssigningApprovals();
            taskRow.setLevel(configurationStep.getLevel());
            taskRow.setStatus(Status.DRAFT);
            taskRow.setApprovalType(ApprovalType.PURCHASE_ORDER);
            taskRow.setSource(ApprovalSource.AUTO);
            taskRow.setEmployeeGroup(configurationStep.getEmployeeGroup());
            approvalTiersDataset.add(taskRow);
        }
        workflowGrid.setItems(approvalTiersDataset);
    }

    private void insertManualApprovalTierLine() {
        int highestCurrentSequence = approvalTiersDataset.stream()
                .map(AssigningApprovals::getLevel)
                .max(Integer::compareTo)
                .orElse(0);

        AssigningApprovals manualStep = new AssigningApprovals();
        manualStep.setLevel(highestCurrentSequence + 1);
        manualStep.setStatus(Status.DRAFT);
        manualStep.setApprovalType(ApprovalType.PURCHASE_ORDER);
        manualStep.setSource(ApprovalSource.MANUAL);

        approvalTiersDataset.add(manualStep);
        workflowGrid.setItems(approvalTiersDataset);
    }

    private void resequenceWorkflowTiers() {
        int rankingIndex = 1;
        for (AssigningApprovals tier : approvalTiersDataset) {
            tier.setLevel(rankingIndex++);
        }
    }

    private void executeCommitAndSubmitWorkflow() {
        if (approvalTiersDataset.isEmpty()) {
            Notification.show("Sourcing Constraint: You must allocate at least one configuration approval group.", 3000,
                    Position.TOP_CENTER);
            return;
        }

        for (AssigningApprovals validationRow : approvalTiersDataset) {
            if (validationRow.getEmployeeGroup() == null) {
                Notification.show("Missing profile role group assignment at sequence index " + validationRow.getLevel(),
                        4000, Position.TOP_CENTER);
                return;
            }
        }

        try {
            Employee loggedInBuyerActor = securityService.getLoggedInUser().getEmployee();

            poHeader.setCreatedBy(loggedInBuyerActor);
            poHeader.setStatus(Status.WAITING_APPROVAL);
            poHeader.setLevel(approvalTiersDataset.size());
            poHeaderService.savePurchaseOrderHeader(poHeader);

            int finalSequenceCounter = 1;
            for (AssigningApprovals approvalTask : approvalTiersDataset) {
                approvalTask.setReferenceId(poHeader.getPurchaseOrderId());
                approvalTask.setStatus(Status.DRAFT);
                approvalTask.setAssignedDate(LocalDate.now());
                approvalTask.setAssignedBy(loggedInBuyerActor);
                approvalTask.setLevel(finalSequenceCounter++);

                assigningApprovalsService.addApprovals(approvalTask, loggedInBuyerActor);
            }

            Notification.show(
                    "Consolidated Purchase Order matrix finalized and successfully routed to workflow queues!", 4000,
                    Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate("purchase-order"));

        } catch (Exception ex) {
            Notification.show("Failed to commit approval matrix track: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }
}