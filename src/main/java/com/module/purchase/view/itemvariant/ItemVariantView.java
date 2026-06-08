package com.module.purchase.view.itemvariant;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.ItemVariantService;
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

@Route(value = "item-variant", layout = MainLayout.class)
@PermitAll
public class ItemVariantView extends VerticalLayout {

    private final ItemVariantService itemVariantService;

    private final Grid<ItemVariant> itemVariantGrid =
            new Grid<>(ItemVariant.class, false);

    private final TextField variantIdField =
            new TextField("Variant ID");

    private final ComboBox<Item> itemField =
            new ComboBox<>("Item");

    private final TextField specificationField =
            new TextField("Specification");

    private final ComboBox<String> activeField =
            new ComboBox<>("Active");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private ItemVariant currentFilter = new ItemVariant();

    public ItemVariantView(
            ItemVariantService itemVariantService,
            ItemService itemService,
            SecurityService securityService) {

        this.itemVariantService = itemVariantService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // HEADER
        H2 title = new H2("Item Variant List");

        Button addButton = new Button("Add Variant");

        addButton.addClickListener(e -> {
            ItemVariantForm form =
                    new ItemVariantForm(
                            itemVariantService,
                            itemService,
                            securityService);

            form.open();
        });

        addButton.setVisible(
                securityService.canAccessView("item-variant-form"));

        HorizontalLayout headerLayout =
                new HorizontalLayout(title, addButton);

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(
                JustifyContentMode.BETWEEN);

        // FILTERS
        itemField.setItems(itemService.getItems());
        itemField.setItemLabelGenerator(Item::getItemName);
        itemField.setClearButtonVisible(true);

        activeField.setItems("Yes", "No");
        activeField.setClearButtonVisible(true);

        Button searchButton =
                new Button("Search", e -> applyFilter());

        Button clearButton =
                new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout =
                new HorizontalLayout(
                        variantIdField,
                        itemField,
                        specificationField,
                        activeField,
                        searchButton,
                        clearButton);

        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();

        // GRID
        itemVariantGrid.addColumn(ItemVariant::getId)
                .setHeader("ID")
                .setAutoWidth(true);

        itemVariantGrid.addColumn(variant ->
                variant.getItem() == null
                        ? ""
                        : variant.getItem().getItemName())
                .setHeader("Item")
                .setAutoWidth(true);

        itemVariantGrid.addColumn(ItemVariant::getSpecification)
                .setHeader("Specification")
                .setAutoWidth(true);

        itemVariantGrid.addColumn(ItemVariant::getEstimatedUnitPrice)
                .setHeader("Estimated Price")
                .setAutoWidth(true);

        itemVariantGrid.addColumn(variant ->
                Boolean.TRUE.equals(variant.getActive())
                        ? "Yes"
                        : "No")
                .setHeader("Active")
                .setAutoWidth(true);

        itemVariantGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES);

        itemVariantGrid.setSizeFull();

        itemVariantGrid.addItemDoubleClickListener(event -> {

            ItemVariant variant = event.getItem();

            getUI().ifPresent(ui ->
                    ui.navigate(
                            "item-variant-details/"
                                    + variant.getId()));
        });

        // PAGE SIZE
        ComboBox<Integer> pageSizeField =
                new ComboBox<>();

        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {

            pageSize = e.getValue();
            currentPage = 0;

            loadVariants();
        });

        // PAGINATION
        Button previousButton =
                new Button("Previous", e -> {

                    if (currentPage > 0) {

                        currentPage--;

                        loadVariants();
                    }
                });

        Button nextButton =
                new Button("Next", e -> {

                    currentPage++;

                    loadVariants();
                });

        HorizontalLayout paginationLayout =
                new HorizontalLayout(
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

        loadVariants();

        add(
                headerLayout,
                filterLayout,
                itemVariantGrid,
                paginationLayout);

        expand(itemVariantGrid);
    }

    private void loadVariants() {

        Page<ItemVariant> page =
                itemVariantService.getAllItemVariants(
                        currentFilter,
                        currentPage,
                        pageSize);

        itemVariantGrid.setItems(page.getContent());

        pageInfo.setText(
                "Page "
                        + (currentPage + 1)
                        + " of "
                        + page.getTotalPages());
    }

    private void applyFilter() {

        Long variantId = null;

        if (!variantIdField.getValue().isEmpty()) {

            variantId = Long.valueOf(
                    variantIdField.getValue().trim());
        }

        currentFilter = new ItemVariant();

        currentFilter.setId(variantId);

        currentFilter.setItem(
                itemField.getValue());

        currentFilter.setSpecification(
                specificationField.getValue());

        if (activeField.getValue() != null) {

            currentFilter.setActive(
                    activeField.getValue()
                            .equals("Yes"));
        }

        currentPage = 0;

        loadVariants();
    }

    private void clearFilter() {

        variantIdField.clear();
        itemField.clear();
        specificationField.clear();
        activeField.clear();

        currentFilter = new ItemVariant();

        currentPage = 0;

        loadVariants();
    }
}