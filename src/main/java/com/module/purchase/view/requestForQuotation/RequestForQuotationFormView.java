package com.module.purchase.view.requestForQuotation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.service.RequestForQuotationService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
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

@Route(value = "rfq-form", layout = MainLayout.class)
@PermitAll
public class RequestForQuotationFormView extends VerticalLayout implements HasUrlParameter<Long> {

    private final RequestForQuotationService rfqService;
    private final PurchaseRequestLineService prLineService;
    private final CategoryService categoryService;
    private final SecurityService securityService;

    private RequestForQuotation editingRfq;
    private Category activeCategoryFilter;

    private final VerticalLayout demandSourcingSection = new VerticalLayout();
    private final Grid.Column<RequestForQuotationLine> actionColumn;

    private final DatePicker requestedDate = new DatePicker("Requested Date");
    private final DatePicker requestEndDate = new DatePicker("Quotation Closing / End Date");

    private final ComboBox<Category> categorySelector = new ComboBox<>("RFQ Item Category");
    private final Grid<PurchaseRequestLine> pendingPrLinesGrid = new Grid<>(PurchaseRequestLine.class, false);
    private final Button importSelectedLinesBtn = new Button("Add Selected Lines to RFQ");

    private final Grid<RequestForQuotationLine> rfqLinesGrid = new Grid<>(RequestForQuotationLine.class, false);
    private final List<RequestForQuotationLine> rfqWorkingLinesList = new ArrayList<>();

    private final List<PurchaseRequestLine> temporaryImportedPrLinesList = new ArrayList<>();

    private final Button saveDraftBtn = new Button("Save as Draft");
    private final Button saveAndOpenBtn = new Button("Save and Open");
    private final Button deleteRfqBtn = new Button("Delete RFQ");
    private final Button cancelBtn = new Button("Back");

    public RequestForQuotationFormView(RequestForQuotationService rfqService, PurchaseRequestLineService prLineService,
            CategoryService categoryService, SecurityService securityService) {
        this.rfqService = rfqService;
        this.prLineService = prLineService;
        this.categoryService = categoryService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        rfqLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName()
                : "").setHeader("Sourced Item").setAutoWidth(true);
        rfqLinesGrid.addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification").setAutoWidth(true);
        rfqLinesGrid.addColumn(RequestForQuotationLine::getRequestedQuantity).setHeader("Quantity Locked")
                .setWidth("140px");

        actionColumn = rfqLinesGrid.addComponentColumn(line -> {
            Button removeBtn = new Button(VaadinIcon.TRASH.create());
            removeBtn.addThemeName("error small tertiary");
            removeBtn.addClickListener(c -> {
                try {
                    if (this.editingRfq != null && this.editingRfq.getId() != null) {
                        List<PurchaseRequestLine> linkedPrLines = prLineService.getRequestForQuotation(editingRfq);
                        for (PurchaseRequestLine prLine : linkedPrLines) {
                            if (prLine.getItemVariant() != null
                                    && prLine.getItemVariant().getId().equals(line.getItemVariant().getId())) {
                                prLine.setRequestForQuotation(null);
                                prLineService.updatePurchaseRequestLine(prLine, securityService.getLoggedInUser().getEmployee());
                            }
                        }
                    }
                } catch (Exception ex) {
                    Notification.show("Notice: Transient element cleared.");
                }

                temporaryImportedPrLinesList.removeIf(prLine
                        -> prLine.getItemVariant() != null
                        && prLine.getItemVariant().getId().equals(line.getItemVariant().getId())
                );

                rfqWorkingLinesList.remove(line);
                rfqLinesGrid.getDataProvider().refreshAll();

                toggleCategorySelectorState();
                loadApprovedPrLinesByCategory(activeCategoryFilter);
            });
            return removeBtn;
        }).setHeader("Action").setWidth("90px");

        buildUI();
    }

    private void buildUI() {
        VerticalLayout scrollContent = new VerticalLayout();
        scrollContent.setWidthFull();
        scrollContent.setPadding(true);
        scrollContent.setSpacing(true);

        H2 formTitle = new H2("Request for Quotation ");

        requestedDate.setValue(LocalDate.now());
        requestedDate.setReadOnly(true);

        requestEndDate.setValue(LocalDate.now().plusDays(7));
        requestEndDate.setMin(LocalDate.now().plusDays(1));
        requestEndDate.setRequired(true);

        FormLayout headerLayout = new FormLayout(requestedDate, requestEndDate);
        headerLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        categorySelector.setItems(categoryService.getCategories());
        categorySelector.setItemLabelGenerator(Category::getCategoryName);
        categorySelector.setPlaceholder("Select a category");
        categorySelector.setWidth("300px");
        categorySelector.setRequiredIndicatorVisible(true); 
        categorySelector.addValueChangeListener(event -> {
            this.activeCategoryFilter = event.getValue();
            loadApprovedPrLinesByCategory(activeCategoryFilter);
        });

        importSelectedLinesBtn.addThemeName("primary success");
        importSelectedLinesBtn.setIcon(VaadinIcon.DOWNLOAD.create());
        importSelectedLinesBtn.addClickListener(e -> batchImportSelectedPrLinesToRfq());

        HorizontalLayout pickerHeaderContainer = new HorizontalLayout(categorySelector, importSelectedLinesBtn);
        pickerHeaderContainer.setAlignItems(Alignment.END);

        pendingPrLinesGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        pendingPrLinesGrid.addColumn(
                line -> line.getPurchaseRequestHeader() != null ? line.getPurchaseRequestHeader().getPurchaseRequestId()
                : "-")
                .setHeader("PR ID").setWidth("80px");
        pendingPrLinesGrid.addColumn(line -> line.getItemVariant() != null && line.getItemVariant().getItem() != null
                ? line.getItemVariant().getItem().getItemName()
                : "").setHeader("Item Name").setAutoWidth(true);
        pendingPrLinesGrid
                .addColumn(line -> line.getItemVariant() != null ? line.getItemVariant().getSpecification() : "")
                .setHeader("Specification").setAutoWidth(true);
        pendingPrLinesGrid.addColumn(
                line -> line.getApprovedQuantity() != null ? line.getApprovedQuantity() : line.getRequestedQuantity())
                .setHeader("Approved Qty").setWidth("120px");
        pendingPrLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        pendingPrLinesGrid.setAllRowsVisible(true);
        pendingPrLinesGrid.setWidthFull();

        demandSourcingSection.setWidthFull();
        demandSourcingSection.setPadding(false);
        demandSourcingSection.setSpacing(true);
        demandSourcingSection.add(new H3("Pending Purchase Requests"), pickerHeaderContainer, pendingPrLinesGrid);

        rfqLinesGrid.setAllRowsVisible(true);
        rfqLinesGrid.setWidthFull();
        rfqLinesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        saveDraftBtn.addThemeName("secondary contrast");
        saveDraftBtn.addClickListener(e -> persistFormTransaction(RequestForQuotationStatus.DRAFT));

        saveAndOpenBtn.addThemeName("primary success");
        saveAndOpenBtn.addClickListener(e -> persistFormTransaction(RequestForQuotationStatus.OPEN));

        deleteRfqBtn.addThemeName("error primary");
        deleteRfqBtn.setIcon(VaadinIcon.TRASH.create());
        deleteRfqBtn.setVisible(false);
        deleteRfqBtn.addClickListener(e -> executeFullRfqDeletionRoutine());

        cancelBtn.addThemeName("tertiary");
        cancelBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("request-for-quotation")));

        HorizontalLayout footerActions = new HorizontalLayout(saveDraftBtn, saveAndOpenBtn, deleteRfqBtn, cancelBtn);
        footerActions.setSpacing(true);

        scrollContent.add(formTitle, headerLayout, new Hr(),
                demandSourcingSection, new H3("Request for Quotation Lines"), rfqLinesGrid, footerActions);

        Scroller viewScroller = new Scroller(scrollContent);
        viewScroller.setSizeFull();
        add(viewScroller);
    }

    private void loadApprovedPrLinesByCategory(Category category) {
        if (category == null) {
            pendingPrLinesGrid.setItems(new ArrayList<>());
            return;
        }

        List<PurchaseRequestLine> matchedLines = prLineService.getPurchaseLinesByCategory(category);

        List<PurchaseRequestLine> filteredLines = matchedLines.stream()
                .filter(prLine -> temporaryImportedPrLinesList.stream()
                .noneMatch(imported -> imported.getId().equals(prLine.getId())))
                .toList();

        pendingPrLinesGrid.setItems(filteredLines);
    }

    private void batchImportSelectedPrLinesToRfq() {
        if (categorySelector.isEmpty()) {
            Notification.show("Please select a Category before importing lines.", 3000, Position.MIDDLE);
            return;
        }

        Set<PurchaseRequestLine> selectedPrLines = pendingPrLinesGrid.getSelectedItems();
        if (selectedPrLines.isEmpty()) {
            Notification.show("Please select at least one checkbox entry line.", 3000, Position.MIDDLE);
            return;
        }

        for (PurchaseRequestLine prLine : selectedPrLines) {
            double incomingQty = prLine.getApprovedQuantity() != null ? prLine.getApprovedQuantity()
                    : prLine.getRequestedQuantity();

            temporaryImportedPrLinesList.add(prLine);

            RequestForQuotationLine existingLine = rfqWorkingLinesList.stream()
                    .filter(line -> line.getItemVariant().getId().equals(prLine.getItemVariant().getId()))
                    .findFirst().orElse(null);

            if (existingLine != null) {
                existingLine.setRequestedQuantity(existingLine.getRequestedQuantity() + incomingQty);
            } else {
                RequestForQuotationLine newRfqLine = new RequestForQuotationLine();
                newRfqLine.setItemVariant(prLine.getItemVariant());
                newRfqLine.setRequestedQuantity(incomingQty);
                if (this.editingRfq != null) {
                    newRfqLine.setRequestForQuotation(editingRfq);
                }
                rfqWorkingLinesList.add(newRfqLine);
            }
        }

        rfqLinesGrid.setItems(rfqWorkingLinesList);
        rfqLinesGrid.getDataProvider().refreshAll();

        toggleCategorySelectorState();

        pendingPrLinesGrid.deselectAll();
        loadApprovedPrLinesByCategory(activeCategoryFilter);
        Notification.show("Items imported to active workspace and consolidated successfully.");
    }

    private void toggleCategorySelectorState() {
        if (!rfqWorkingLinesList.isEmpty()) {
            categorySelector.setReadOnly(true);
        } else {
            if (this.editingRfq == null || this.editingRfq.getStatus() == RequestForQuotationStatus.DRAFT) {
                categorySelector.setReadOnly(false);
            }
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long id) {
        temporaryImportedPrLinesList.clear();

        if (id != null) {
            rfqService.getRequestForQuotationById(id).ifPresent(rfq -> {
                this.editingRfq = rfq;
                requestedDate.setValue(rfq.getRequestedDate());
                requestEndDate.setValue(rfq.getRequestEndDate());

                // Show preserved category assignment inside database
                categorySelector.setValue(rfq.getCategory());

                rfqWorkingLinesList.clear();
                rfqWorkingLinesList.addAll(rfqService.getLinesByRfqId(rfq.getId()));
                rfqLinesGrid.setItems(rfqWorkingLinesList);

                List<PurchaseRequestLine> linkedDbLines = prLineService.getRequestForQuotation(editingRfq);
                temporaryImportedPrLinesList.addAll(linkedDbLines);

                switch (rfq.getStatus()) {
                    case DRAFT -> {
                        requestEndDate.setReadOnly(false);
                        demandSourcingSection.setVisible(true);
                        actionColumn.setVisible(true);

                        saveDraftBtn.setVisible(true);
                        saveAndOpenBtn.setVisible(true);
                        deleteRfqBtn.setVisible(true);
                        toggleCategorySelectorState();
                    }
                    default -> {
                        lockDownViewEntirely();
                        categorySelector.setReadOnly(true);
                    }
                }
            });
        } else {
            this.editingRfq = null;
            categorySelector.clear();
            categorySelector.setReadOnly(false);
            rfqWorkingLinesList.clear();
            rfqLinesGrid.setItems(rfqWorkingLinesList);
            requestEndDate.setReadOnly(false);
            demandSourcingSection.setVisible(true);
            actionColumn.setVisible(true);

            saveDraftBtn.setVisible(true);
            saveAndOpenBtn.setVisible(true);
            deleteRfqBtn.setVisible(false);
        }
    }

    private void lockDownViewEntirely() {
        requestEndDate.setReadOnly(true);
        categorySelector.setReadOnly(true);
        demandSourcingSection.setVisible(false);
        actionColumn.setVisible(false);
        saveDraftBtn.setVisible(false);
        saveAndOpenBtn.setVisible(false);
        deleteRfqBtn.setVisible(false);
    }

    private void persistFormTransaction(RequestForQuotationStatus targetedStatus) {
        if (categorySelector.isEmpty() || requestEndDate.isEmpty() || rfqWorkingLinesList.isEmpty()) {
            Notification.show("Validation Fault: Category, valid deadline date, and item lines are required.", 4000,
                    Position.MIDDLE);
            return;
        }

        try {
            Employee actor = securityService.getLoggedInUser().getEmployee();
            LocalDate dateCommitValue = (targetedStatus == RequestForQuotationStatus.OPEN) ? LocalDate.now() : requestedDate.getValue();

            if (this.editingRfq == null) {
                RequestForQuotation newRfq = new RequestForQuotation();
                newRfq.setRequestedDate(dateCommitValue);
                newRfq.setRequestEndDate(requestEndDate.getValue());
                newRfq.setStatus(targetedStatus);

                newRfq.setCategory(categorySelector.getValue());

                this.editingRfq = rfqService.addRequestForQuotation(newRfq, actor);
            } else {
                editingRfq.setRequestedDate(dateCommitValue);
                editingRfq.setRequestEndDate(requestEndDate.getValue());
                editingRfq.setStatus(targetedStatus);
                editingRfq.setCategory(categorySelector.getValue());
                rfqService.updateRequestForQuotation(editingRfq, actor);
            }

            for (PurchaseRequestLine prLine : temporaryImportedPrLinesList) {
                if (prLine.getRequestForQuotation() == null) {
                    prLine.setRequestForQuotation(editingRfq);
                    prLineService.updatePurchaseRequestLine(prLine, actor);
                }
            }

            for (RequestForQuotationLine uiLine : rfqWorkingLinesList) {
                if (uiLine.getRequestForQuotation() == null) {
                    uiLine.setRequestForQuotation(editingRfq);
                    rfqService.addRfqLine(uiLine);
                }
            }

            Notification.show("Request for Quotation saved under status: " + targetedStatus, 3000, Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation"));

        } catch (Exception exception) {
            Notification.show("Error processing database save: " + exception.getMessage(), 5000, Position.MIDDLE);
        }
    }

    private void executeFullRfqDeletionRoutine() {
        if (this.editingRfq == null || this.editingRfq.getId() == null) {
            return;
        }
        try {
            Employee actor = securityService.getLoggedInUser().getEmployee();
            rfqService.deleteRequestForQuotationById(editingRfq.getId(), actor);
            Notification.show("Draft RFQ dropped. Source lines unmapped safely.", 4000, Position.TOP_CENTER);
            getUI().ifPresent(ui -> ui.navigate("request-for-quotation"));
        } catch (Exception ex) {
            Notification.show("Deletion execution faulted: " + ex.getMessage(), 5000, Position.MIDDLE);
        }
    }
}
