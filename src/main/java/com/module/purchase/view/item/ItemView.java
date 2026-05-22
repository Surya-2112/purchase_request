package com.module.purchase.view.item;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Item;
import com.module.purchase.service.ItemService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "item", layout = MainLayout.class)
@PermitAll
public class ItemView extends VerticalLayout {

    private final ItemService itemService;

  //  private final SecurityService securityService;

    private final Grid<Item> itemGrid = new Grid<>(Item.class, false);

    private final TextField itemIdField = new TextField("Item ID");
    private final TextField itemNameField = new TextField("Item Name");
    private final TextField itemCodeField = new TextField("Item Code");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private Item currentFilter = new Item();

    public ItemView(ItemService itemService, SecurityService securityService) {

        this.itemService = itemService;
  //      this.securityService=securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // HEADER
        H2 title = new H2("Item List");

        Button addButton = new Button("Add Item");
        addButton.addClickListener(e -> {
            ItemForm form = new ItemForm(itemService,securityService);
            form.open();
        });

        HorizontalLayout headerLayout = new HorizontalLayout(title, addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // FILTER
        Button searchButton = new Button("Search", e -> applyFilter());
        Button clearButton = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout = new HorizontalLayout(
                itemIdField,
                itemNameField,
                itemCodeField,
                searchButton,
                clearButton
        );

        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();

        // PAGE SIZE
        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadItems();
        });

        // PAGINATION BUTTONS
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

        HorizontalLayout paginationLayout = new HorizontalLayout(
                                previousButton,
                                pageInfo,
                                nextButton,
                                new Span("Page Size"),
                                pageSizeField);

        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        // GRID
        itemGrid.addComponentColumn(item -> {

            Button idButton = new Button(String.valueOf(item.getItemId()));

            idButton.addClickListener(e -> {
                getUI().ifPresent(ui ->
                        ui.navigate("item-details/" + item.getItemId())
                );
            });

            return idButton;

        }).setHeader("Item ID").setAutoWidth(true);

        itemGrid.addColumn(Item::getItemName)
                .setHeader("Item Name")
                .setAutoWidth(true);

        itemGrid.addColumn(Item::getItemCode)
                .setHeader("Item Code")
                .setAutoWidth(true);

        itemGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        itemGrid.setSizeFull();

        itemGrid.addItemClickListener(event -> {
            Item item = event.getItem();

            Notification.show(
                    "Item: " + item.getItemName(),
                    3000,
                    Notification.Position.TOP_CENTER
            );
        });

        // LOAD
        loadItems();

        add(headerLayout, filterLayout, itemGrid, paginationLayout);
        expand(itemGrid);
    }

    private void loadItems() {

        Page<Item> page = itemService.getAllItems(
                currentFilter,
                currentPage,
                pageSize
        );

        itemGrid.setItems(page.getContent());

        pageInfo.setText(
                "Page " + (currentPage + 1)
                        + " of " + page.getTotalPages()
        );
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

        currentPage = 0;
        loadItems();
    }

    private void clearFilter() {

        itemIdField.clear();
        itemNameField.clear();
        itemCodeField.clear();

        currentFilter = new Item();
        currentPage = 0;

        loadItems();
    }
}