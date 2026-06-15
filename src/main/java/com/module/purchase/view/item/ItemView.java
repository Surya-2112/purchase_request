package com.module.purchase.view.item;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.Unit;
import com.module.purchase.entity.Needs;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EntityType;
import com.module.purchase.service.AssigningApprovalsService;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.ItemVariantService;
import com.module.purchase.service.UnitService;
import com.module.purchase.service.NeedsService;
import com.module.purchase.service.PurchaseRequestHeaderService;
import com.module.purchase.service.PurchaseRequestLineService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "item", layout = MainLayout.class)
@PermitAll
public class ItemView extends VerticalLayout {

    private final ItemService itemService;
    private final ItemVariantService itemVariantService;
    private final CategoryService categoryService;
    private final UnitService unitService;
    private final SecurityService securityService;
    private final NeedsService needsService;
    private final PurchaseRequestHeaderService headerService;
    private final PurchaseRequestLineService prLineService;
    private final AssigningApprovalsService assigningApprovalsService;

    private final VerticalLayout catalogTabContent = new VerticalLayout();
    private final VerticalLayout requestsTabContent = new VerticalLayout();
    private final Tabs viewTabs = new Tabs();

    private final Grid<Item> itemGrid = new Grid<>(Item.class, false);
    private final TextField itemIdField = new TextField("Item ID");
    private final TextField itemNameField = new TextField("Item Name");
    private final TextField itemCodeField = new TextField("Item Code");
    private final ComboBox<Category> categoryField = new ComboBox<>("Category");
    private final ComboBox<Unit> unitField = new ComboBox<>("Unit");
    private final Span pageInfo = new Span();
    private int currentPage = 0;
    private int pageSize = 25;
    private Item currentFilter = new Item();

    private final Grid<Needs> adHocNeedsGrid = new Grid<>(Needs.class, false);

    public ItemView(ItemService itemService, ItemVariantService itemVariantService,CategoryService categoryService,
            UnitService unitService, SecurityService securityService, NeedsService needsService,
            AssigningApprovalsService assigningApprovalsService,
            PurchaseRequestHeaderService headerService, PurchaseRequestLineService prLineService) {

        this.itemService = itemService;
        this.itemVariantService = itemVariantService;
        this.categoryService = categoryService;
        this.unitService = unitService;
        this.securityService = securityService;
        this.assigningApprovalsService=assigningApprovalsService;
        this.needsService = needsService;
        this.headerService = headerService;
        this.prLineService = prLineService;

        setSizeFull();
        setPadding(true);
        setSpacing(false);

        setupTabsStructureConfiguration();
        buildMasterCatalogTabPane();
        buildAdHocRequestsTabPane();

        requestsTabContent.setVisible(false);
        add(viewTabs, catalogTabContent, requestsTabContent);
    }

    private void setupTabsStructureConfiguration() {
        Tab officialCatalogTab = new Tab(VaadinIcon.LIST_SELECT.create(), new Span("Master Catalog"));
        Tab adHocRequestsTab = new Tab(VaadinIcon.WARNING.create(), new Span("Requested Items"));
        
        viewTabs.add(officialCatalogTab, adHocRequestsTab);
        viewTabs.setWidthFull();
        viewTabs.getStyle().set("margin-bottom", "15px");

        viewTabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            if (selectedTab == officialCatalogTab) {
                catalogTabContent.setVisible(true);
                requestsTabContent.setVisible(false);
                loadItems();
            } else {
                catalogTabContent.setVisible(false);
                requestsTabContent.setVisible(true);
                loadActiveAdHocNeedsLedger();
            }
        });
    }

    private void buildMasterCatalogTabPane() {
        catalogTabContent.setSizeFull();
        catalogTabContent.setPadding(false);

        H2 title = new H2("Item List");
        Button addButton = new Button("Add Item");
        addButton.addClickListener(e -> {
            ItemForm form = new ItemForm(itemService, itemVariantService, categoryService, unitService, securityService);
            form.open();
        });
        addButton.setVisible(securityService.canAccessView("item-form"));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        itemIdField.setWidth("75px");

        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);
        unitField.setItems(unitService.getAllUnits());
        unitField.setItemLabelGenerator(Unit::getName);
        unitField.setWidth("75px");
        Button searchButton = new Button("Search", e -> applyFilter());
        Button clearButton = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout = new HorizontalLayout(
                itemIdField, itemNameField, itemCodeField, categoryField, unitField, searchButton, clearButton);
        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();

        itemGrid.addColumn(Item::getItemId).setHeader("Item ID").setAutoWidth(true);
        itemGrid.addColumn(Item::getItemName).setHeader("Item Name").setAutoWidth(true);
        itemGrid.addColumn(Item::getItemCode).setHeader("Item Code").setAutoWidth(true);
        itemGrid.addColumn(item -> item.getCategory() == null ? "" : item.getCategory().getCategoryName()).setHeader("Category").setAutoWidth(true);
        itemGrid.addColumn(item -> item.getUnit() == null ? "" : item.getUnit().getName()).setHeader("Unit").setAutoWidth(true);
        itemGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        itemGrid.setSizeFull();

        itemGrid.addItemDoubleClickListener(event -> {
            Item item = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("item-details/" + item.getItemId()));
        });

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);
        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadItems();
        });

        Button previousButton = new Button("Previous", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadItems();
            }
        });
        Button nextButton = new Button("Next", e -> {
            currentPage++;
            loadItems();
        });

        HorizontalLayout paginationLayout = new HorizontalLayout(previousButton, pageInfo, nextButton, new Span("Page Size"), pageSizeField);
        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        catalogTabContent.add(headerLayout, filterLayout, itemGrid, paginationLayout);
        catalogTabContent.expand(itemGrid);
        loadItems();
    }

    private void buildAdHocRequestsTabPane() {
        requestsTabContent.setSizeFull();
        requestsTabContent.setPadding(false);

        H2 Title = new H2("Requested Items");
        requestsTabContent.add(Title);

        adHocNeedsGrid.addColumn(Needs::getId).setHeader("Need Entry ID").setWidth("110px");
        adHocNeedsGrid.addColumn(Needs::getNeedLine).setHeader("Requested Specification").setAutoWidth(true);
        adHocNeedsGrid.addColumn(Needs::getRefId).setHeader("Reference ID").setWidth("160px");

        adHocNeedsGrid.addComponentColumn(need -> {
            Button processBtn = new Button("Assign Line", VaadinIcon.DATABASE.create());
            processBtn.addThemeName("primary small success");
            processBtn.addClickListener(e -> openCatalogPromotionWizardDialogueModal(need));
            return processBtn;
        }).setHeader("Action").setWidth("240px");

        adHocNeedsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        adHocNeedsGrid.setSizeFull();
        
        requestsTabContent.add(adHocNeedsGrid);
        requestsTabContent.expand(adHocNeedsGrid);
    }

    private void loadItems() {
        Page<Item> page = itemService.getAllItems(currentFilter, currentPage, pageSize);
        itemGrid.setItems(page.getContent());
        pageInfo.setText("Page " + (currentPage + 1) + " of " + page.getTotalPages());
    }

    private void loadActiveAdHocNeedsLedger() {
        adHocNeedsGrid.setItems(needsService.getNeedsByDomain(EntityType.ITEM));
    }

    private void openCatalogPromotionWizardDialogueModal(Needs selectedNeed) {
        Dialog promotionModal = new Dialog();
        promotionModal.setHeaderTitle("Unlisted Item Allocation");
        promotionModal.setWidth("520px");

        String rawPayload = selectedNeed.getNeedLine(); 
        double requestedQty = 1.0;
        try {
            requestedQty = Double.parseDouble(extractPayloadValueByKey(rawPayload, "Qty"));
        } catch (Exception skipped) {}

        RadioButtonGroup<String> resolutionModeRadio = new RadioButtonGroup<>();
        resolutionModeRadio.setLabel("Assign Item");
        resolutionModeRadio.setItems("Map to Existing Catalog Item", "Register New Item");
        resolutionModeRadio.setValue("Map to Existing Catalog Item");

        ComboBox<Item> existingItemBox = new ComboBox<>("Select Item");
        existingItemBox.setItems(itemService.getItems());
        existingItemBox.setItemLabelGenerator(Item::getItemName);
        existingItemBox.setWidthFull();

        ComboBox<ItemVariant> existingVariantBox = new ComboBox<>("Select Item Variant");
        existingVariantBox.setWidthFull();
        existingVariantBox.setItemLabelGenerator(v -> v.getSpecification() != null ? v.getSpecification() : "Base Specification");

        existingItemBox.addValueChangeListener(ev -> {
            if (ev.getValue() != null) {
                existingVariantBox.setItems(itemVariantService.getItemVariantsByItem(ev.getValue()));
            } else {
                existingVariantBox.clear();
            }
        });

        VerticalLayout existingModeLayout = new VerticalLayout(existingItemBox, existingVariantBox);
        existingModeLayout.setPadding(false);

        TextField officialNameField = new TextField("Item Name");
        officialNameField.setRequired(true);
        TextField officialCodeField = new TextField("Item Code");
        officialCodeField.setRequired(true);
        TextField officialSpecField = new TextField("Variant Specification");
        officialSpecField.setRequired(true);
        NumberField estimatedPriceField = new NumberField("Estimated Unit Price");
        estimatedPriceField.setValue(0.0);
        estimatedPriceField.setMin(1.0);
        estimatedPriceField.setRequired(true);

        ComboBox<Category> targetCatBox = new ComboBox<>("Category");
        targetCatBox.setItems(categoryService.getCategories());
        targetCatBox.setItemLabelGenerator(Category::getCategoryName);
        targetCatBox.setRequired(true);

        ComboBox<Unit> targetUnitBox = new ComboBox<>("Unit ");
        targetUnitBox.setItems(unitService.getAllUnits());
        targetUnitBox.setItemLabelGenerator(Unit::getName);
        targetUnitBox.setRequired(true);

        FormLayout newModeFormLayout = new FormLayout(
            officialNameField, officialCodeField, officialSpecField, 
            estimatedPriceField, targetCatBox, targetUnitBox
        );
        newModeFormLayout.setWidthFull();
        newModeFormLayout.setVisible(false); 

        resolutionModeRadio.addValueChangeListener(e -> {
            boolean isNewMode = "Register New Item".equals(e.getValue());
            existingModeLayout.setVisible(!isNewMode);
            newModeFormLayout.setVisible(isNewMode);
        });

        VerticalLayout dialogBodyContent = new VerticalLayout(resolutionModeRadio, new Hr(), existingModeLayout, newModeFormLayout);
        dialogBodyContent.setPadding(false);

        Button executePromotionBtn = new Button("Add Item & Move To Approvals");
        executePromotionBtn.addThemeName("primary success");
        
        final double finalQty = requestedQty; 
        executePromotionBtn.addClickListener(click -> {
            try {
                Optional<PurchaseRequestHeader> headerOpt = headerService.getPurchaseRequestHeaderById(selectedNeed.getRefId());
                if (headerOpt.isEmpty()) {
                        Notification.show("System Error: Target Purchase Request Header not found.", 4000, Notification.Position.MIDDLE);
                        return;
                }
                PurchaseRequestHeader targetHeader = headerOpt.get();
                ItemVariant targetVariantToAssign = null;

                if ("Map to Existing Catalog Item".equals(resolutionModeRadio.getValue())) {
                    if (existingItemBox.isEmpty() || existingVariantBox.isEmpty()) {
                        Notification.show("Please select both a valid Item and Specification Variant mapping context.", 3000, Notification.Position.MIDDLE);
                        return;
                    }
                    targetVariantToAssign = existingVariantBox.getValue();
                } 
                    else {
                    if (officialNameField.isEmpty() || officialCodeField.isEmpty() || targetCatBox.isEmpty() || targetUnitBox.isEmpty()) {
                        Notification.show("Please fulfill all parameters to insert a new masterc atalog entry.", 3000, Notification.Position.MIDDLE);
                        return;
                    }
                    Item masterItem = new Item();
                    masterItem.setItemName(officialNameField.getValue().trim());
                    masterItem.setItemCode(officialCodeField.getValue().trim().toUpperCase());
                    masterItem.setCategory(targetCatBox.getValue());
                    masterItem.setUnit(targetUnitBox.getValue());
                    masterItem = itemService.saveItem(masterItem);

                    ItemVariant masterVariant = new ItemVariant();
                    masterVariant.setItem(masterItem);
                    masterVariant.setSpecification(officialSpecField.getValue().trim().isEmpty() ? "Default Spec" : officialSpecField.getValue().trim());
                    masterVariant.setEstimatedUnitPrice(estimatedPriceField.getValue() != null ? estimatedPriceField.getValue() : 0.0);
                    targetVariantToAssign = itemVariantService.saveItemVariant(masterVariant);
                }

                PurchaseRequestLine formalLine = new PurchaseRequestLine();
                formalLine.setPurchaseRequestHeader(targetHeader);
                formalLine.setItemVariant(targetVariantToAssign);
                formalLine.setRequestedQuantity(finalQty);
                formalLine.setItemUnitPrice(targetVariantToAssign.getEstimatedUnitPrice());
                formalLine.setItemTotalAmount(targetVariantToAssign.getEstimatedUnitPrice() * finalQty);
                formalLine.setDescription(" ");
                formalLine.setStatus(Status.DRAFT);
                prLineService.addPurchaseRequestLine(formalLine);

                double updatedGlobalTotal = prLineService.getPurchaseRequestLineByHeader(targetHeader)
                        .stream()
                        .mapToDouble(line -> line.getItemTotalAmount() != null ? line.getItemTotalAmount() : 0.0)
                        .sum();
                targetHeader.setTotalAmount(updatedGlobalTotal);

                needsService.resolveAndClearCompletedNeed(EntityType.ITEM, selectedNeed.getId().longValue());


                Employee activeActor = securityService.getLoggedInUser().getEmployee();
                
                if (needsService.getSpecificNeedRecord(EntityType.ITEM, targetHeader.getPurchaseRequestId()).isEmpty()) {
                    targetHeader.setStatus(Status.WAITING_APPROVAL);

                    List<AssigningApprovals> approvals=assigningApprovalsService.getAssigningApprovalByTypeAndReferId(
                        ApprovalType.PURCHASE_REQUEST,targetHeader.getPurchaseRequestId());
                        approvals.get(0).setStatus(Status.WAITING_APPROVAL);
                     assigningApprovalsService.updateApprovals(approvals.get(0),null);

                    Notification.show("Line successfully linked. Document escalated to Manager Queue.");
                } else {
                     Notification.show("Line mapped. Additional pending unresolved items exist on this sheet.");
                }

                headerService.updatePurchaseRequestHeader(targetHeader, activeActor);
                
                promotionModal.close();
                loadActiveAdHocNeedsLedger(); 

            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Mapping Aborted: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        Button cancelBtn = new Button("Cancel", e -> promotionModal.close());
        cancelBtn.addThemeName("tertiary error");

        promotionModal.getFooter().add(cancelBtn, executePromotionBtn);
        promotionModal.add(dialogBodyContent);
        promotionModal.open();
    }

    private String extractPayloadValueByKey(String payload, String key) {
        if (payload == null || !payload.contains(key + ":")) return "";
        try {
            String[] segments = payload.split("\\|");
            for (String segment : segments) {
                if (segment.trim().startsWith(key + ":")) {
                    return segment.split(":")[1].trim();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private void applyFilter() {
        Long itemId = null;
        if (!itemIdField.getValue().isEmpty()) {
            itemId = Long.valueOf(itemIdField.getValue().trim());
        }
        currentFilter = new Item();
        currentFilter.setItemId(itemId);
        currentFilter.setItemName(itemNameField.getValue());
        currentFilter.setItemCode(itemCodeField.getValue());
        currentFilter.setCategory(categoryField.getValue());
        currentFilter.setUnit(unitField.getValue());
        currentPage = 0;
        loadItems();
    }

    private void clearFilter() {
        itemIdField.clear();
        itemNameField.clear();
        itemCodeField.clear();
        categoryField.clear();
        unitField.clear();
        currentFilter = new Item();
        currentPage = 0;
        loadItems();
    }
}