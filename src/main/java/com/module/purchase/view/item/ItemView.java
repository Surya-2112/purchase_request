package com.module.purchase.view.item;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.Unit;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.UnitService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "item", layout = MainLayout.class)
@PermitAll
public class ItemView extends VerticalLayout {

    private final ItemService itemService;

    private final Grid<Item> itemGrid = new Grid<>(Item.class, false);

    private final TextField itemIdField = new TextField("Item ID");
    private final TextField itemNameField = new TextField("Item Name");
    private final TextField itemCodeField = new TextField("Item Code");

    private final ComboBox<Category> categoryField
            = new ComboBox<>("Category");

    private final ComboBox<Unit> unitField
            = new ComboBox<>("Unit");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private Item currentFilter = new Item();

    public ItemView(
            ItemService itemService,
            CategoryService categoryService,
            UnitService unitService,
            SecurityService securityService) {

        this.itemService = itemService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // HEADER
        H2 title = new H2("Item List");

        Button addButton = new Button("Add Item");

        addButton.addClickListener(e -> {
            ItemForm form
                    = new ItemForm(itemService, categoryService, unitService, securityService);
            form.open();
        });

        addButton.setVisible(securityService.canAccessView("item-form"));

        HorizontalLayout headerLayout
                = new HorizontalLayout(title, addButton);

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(
                JustifyContentMode.BETWEEN);

        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);

        unitField.setItems(unitService.getAllUnits());
        unitField.setItemLabelGenerator(Unit::getName);

        // FILTER
        Button searchButton
                = new Button("Search", e -> applyFilter());

        Button clearButton
                = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout
                = new HorizontalLayout(
                        itemIdField,
                        itemNameField,
                        itemCodeField,
                        categoryField,
                        unitField,
                        searchButton,
                        clearButton);

        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();

        // GRID
        itemGrid.addColumn(Item::getItemId)
                .setHeader("Item ID")
                .setAutoWidth(true);

        itemGrid.addColumn(Item::getItemName)
                .setHeader("Item Name")
                .setAutoWidth(true);

        itemGrid.addColumn(Item::getItemCode)
                .setHeader("Item Code")
                .setAutoWidth(true);

        itemGrid.addColumn(item
                -> item.getCategory() == null
                ? ""
                : item.getCategory().getCategoryName())
                .setHeader("Category")
                .setAutoWidth(true);

        itemGrid.addColumn(item
                -> item.getUnit() == null
                ? ""
                : item.getUnit().getName())
                .setHeader("Unit")
                .setAutoWidth(true);

        itemGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES);

        itemGrid.setSizeFull();

        itemGrid.addItemDoubleClickListener(event -> {

            Item item = event.getItem();

            getUI().ifPresent(ui
                    -> ui.navigate(
                            "item-details/"
                            + item.getItemId()));
        });

        // PAGE SIZE
        ComboBox<Integer> pageSizeField
                = new ComboBox<>();

        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {

            pageSize = e.getValue();
            currentPage = 0;

            loadItems();
        });

        // PAGINATION
        Button previousButton
                = new Button("Previous", e -> {

                    if (currentPage > 0) {

                        currentPage--;

                        loadItems();
                    }
                });

        Button nextButton
                = new Button("Next", e -> {

                    currentPage++;

                    loadItems();
                });

        HorizontalLayout paginationLayout
                = new HorizontalLayout(
                        previousButton,
                        pageInfo,
                        nextButton,
                        new Span("Page Size"),
                        pageSizeField);

        paginationLayout.setWidthFull();

        paginationLayout.setJustifyContentMode(
                JustifyContentMode.CENTER);

        paginationLayout.setAlignItems(
                Alignment.CENTER);

        loadItems();

        add(
                headerLayout,
                filterLayout,
                itemGrid,
                paginationLayout);

        expand(itemGrid);
    }

    private void loadItems() {

        Page<Item> page
                = itemService.getAllItems(
                        currentFilter,
                        currentPage,
                        pageSize);

        itemGrid.setItems(page.getContent());

        pageInfo.setText(
                "Page "
                + (currentPage + 1)
                + " of "
                + page.getTotalPages());
    }

    private void applyFilter() {

        Long itemId = null;

        if (!itemIdField.getValue().isEmpty()) {

            itemId = Long.valueOf(
                    itemIdField.getValue().trim());
        }

        currentFilter = new Item();

        currentFilter.setItemId(itemId);
        currentFilter.setItemName(
                itemNameField.getValue());
        currentFilter.setItemCode(
                itemCodeField.getValue());
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
