package com.module.purchase.view.purchaseRequest;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.*;
import com.module.purchase.enums.*;
import com.module.purchase.service.*;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;

import jakarta.annotation.security.PermitAll;

@Route(value = "purchase-request-approval/:id", layout = MainLayout.class)
@PermitAll
public class PurchaseRequestApprovalView extends VerticalLayout implements BeforeEnterObserver {

    private final PurchaseRequestHeaderService headerService;
    private final PurchaseRequestLineService lineService;
    private final RepeatedPeriodService repeatedPeriodService;
    private final AssigningConfigService configService;
    private final AssigningApprovalsService assigningApprovalsService;
    private final SecurityService securityService;

    private PurchaseRequestHeader header;

    private final List<AssigningApprovals> approvals = new ArrayList<>();
    private final Grid<AssigningApprovals> grid = new Grid<>(AssigningApprovals.class, false);

    private final Button addBtn = new Button("Add Approver");
    private final Button saveBtn = new Button("Save");

    public PurchaseRequestApprovalView(PurchaseRequestHeaderService headerService, AssigningConfigService configService,
            PurchaseRequestLineService lineService, RepeatedPeriodService repeatedPeriodService,
            SecurityService securityService, AssigningApprovalsService assigningApprovalsService) {

        this.headerService = headerService;
        this.configService = configService;
        this.lineService = lineService;
        this.repeatedPeriodService = repeatedPeriodService;
        this.securityService = securityService;
        this.assigningApprovalsService = assigningApprovalsService;

        setSizeFull();
        setPadding(true);

        configureGrid();

        addBtn.addClickListener(e -> addLine());
        saveBtn.addClickListener(e -> saveAll());

        add(
                new H2("Purchase Request Approval Setup"),
                new HorizontalLayout(addBtn, saveBtn),
                grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            Long id = Long.valueOf(event.getRouteParameters().get("id").orElseThrow());

            try {
                header = headerService.getPurchaseRequestHeaderById(id).get();
            } catch (Exception ex) {
                event.forwardTo("");
                event.getUI().access(() -> { Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                });
                return;
            }

            if (!header.getCreatedBy().getEmployeeId()
                    .equals(securityService.getLoggedInUser().getEmployee().getEmployeeId())) {
                event.forwardTo("purchase-request");
                event.getUI().access(() -> {
                    Notification.show("Access Denied", 3000, Notification.Position.MIDDLE);
                });
            }
            if (!header.getStatus().equals(Status.DRAFT)) {
                event.forwardTo("purchase-request");
                event.getUI().access(() -> {
                    Notification.show("Purchase Request approval form is not Open", 3000, Notification.Position.MIDDLE);
                });
            }

            loadAutoApprovals();

        } catch (NoSuchElementException | IllegalArgumentException e) {
            Notification.show("Invalid Request Parameter Mapping", 4000, Position.TOP_CENTER);
        } catch (RuntimeException e) {
            Notification.show(e.getMessage(), 4000, Position.TOP_CENTER);
        }
    }

    private void configureGrid() {
        grid.addColumn(AssigningApprovals::getLevel).setHeader("Level");

        grid.addComponentColumn(item -> {
            ComboBox<EmployeeGroup> combo = new ComboBox<>();
            combo.setItemLabelGenerator(EmployeeGroup::getDisplayName);

            combo.addFocusListener(event -> {
                Set<EmployeeGroup> allocatedGroups = approvals.stream()
                        .map(AssigningApprovals::getEmployeeGroup)
                        .filter(Objects::nonNull)
                        .filter(group -> !group.equals(item.getEmployeeGroup()))
                        .collect(Collectors.toSet());

                List<EmployeeGroup> availableOptions = EmployeeGroup.getApprovalGroups().stream()
                        .filter(group -> !allocatedGroups.contains(group))
                        .toList();

                combo.setItems(availableOptions);
            });

            combo.setItems(EmployeeGroup.getApprovalGroups());

            if (item.getSource() == ApprovalSource.AUTO) {
                List<AssigningConfig> configs = configService.getConfigs(
                        ApprovalType.PURCHASE_REQUEST,
                        header != null ? header.getTotalAmount() : 0);

                AssigningConfig config = configs.stream()
                        .filter(c -> c.getLevel() != null && Objects.equals(c.getLevel(), item.getLevel()))
                        .findFirst().orElse(null);

                if (config != null) {
                    combo.setValue(config.getEmployeeGroup());
                    item.setEmployeeGroup(config.getEmployeeGroup());
                }
            } else {
                if (item.getEmployeeGroup() != null) {
                    combo.setValue(item.getEmployeeGroup());
                }
            }

            combo.addValueChangeListener(e -> {
                item.setEmployeeGroup(e.getValue());
                grid.getDataProvider().refreshAll();
            });

            return combo;
        }).setHeader("Approver Group");

        grid.addColumn(a -> a.getSource() != null ? a.getSource().name() : "")
                .setHeader("Source");

        grid.addComponentColumn(item -> {
            Button delete = new Button("Delete");
            delete.addClickListener(e -> {
                approvals.remove(item);
                grid.setItems(approvals);
                grid.getDataProvider().refreshAll();
            });

            if (item.getSource().equals(ApprovalSource.AUTO)) {
                delete.setVisible(false);
            }

            return delete;
        }).setHeader("Action");

        grid.setItems(approvals);
        grid.setAllRowsVisible(true);
    }

    private void loadAutoApprovals() {
        approvals.clear();

        List<AssigningConfig> configs = configService.getConfigs(
                ApprovalType.PURCHASE_REQUEST,
                header.getTotalAmount());

        configs.sort(Comparator.comparing(AssigningConfig::getLevel));

        for (AssigningConfig config : configs) {
            AssigningApprovals approval = new AssigningApprovals();
            approval.setLevel(config.getLevel());
            approval.setStatus(Status.DRAFT);
            approval.setApprovalType(ApprovalType.PURCHASE_REQUEST);
            approval.setSource(ApprovalSource.AUTO);
            approval.setEmployeeGroup(config.getEmployeeGroup());
            approvals.add(approval);
        }
        grid.setItems(approvals);
    }

    private void addLine() {
        int next = approvals.stream()
                .map(AssigningApprovals::getLevel)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        AssigningApprovals a = new AssigningApprovals();
        a.setLevel(next);
        a.setStatus(Status.DRAFT);
        a.setApprovalType(ApprovalType.PURCHASE_REQUEST);
        a.setSource(ApprovalSource.MANUAL);

        approvals.add(a);
        grid.setItems(approvals);
    }

    private void saveAll() {
        if (approvals.isEmpty()) {
            Notification.show("No approval lines");
            return;
        }

        for (AssigningApprovals a : approvals) {
            if (a.getEmployeeGroup() == null) {
                Notification.show("Missing approver group at level " + a.getLevel());
                return;
            }
        }

        Employee currentEmployee = securityService.getLoggedInUser().getEmployee();
        int i = 1;
        for (AssigningApprovals a : approvals) {
            a.setReferenceId(header.getPurchaseRequestId());
            a.setStatus(Status.DRAFT);
            a.setAssignedDate(LocalDate.now());
            a.setAssignedBy(currentEmployee);
            a.setLevel(i++);
            assigningApprovalsService.addApprovals(a, currentEmployee);
        }

        header.setStatus(Status.WAITING_APPROVAL);
        header.setLevel(approvals.size());
        headerService.updatePurchaseRequestHeader(header, currentEmployee);

        for (PurchaseRequestLine line : lineService.getPurchaseRequestLineByHeader(header)) {
            line.setStatus(Status.WAITING_APPROVAL);
            lineService.updatePurchaseRequestLine(line, currentEmployee);
            if (line.getRepeatableId() != null) {
                RepeatedPeriod period = repeatedPeriodService.getRepeatedPeriodById(line.getRepeatableId()).get();
                period.setStatus(RequestForQuotationStatus.OPEN);
                repeatedPeriodService.updateRepeatedPeriod(period, currentEmployee);
            }
        }

        Notification.show("Saved successfully", 3000, Position.TOP_CENTER);
        getUI().ifPresent(ui -> ui.navigate("purchase-request"));
    }
}