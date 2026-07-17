package com.module.purchase.view.itemvariant;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.service.ItemService;
import com.module.purchase.service.ItemVariantService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

    private final Grid<ItemVariant> itemVariantGrid = new Grid<>(ItemVariant.class, false);

    private final TextField variantIdField = new TextField("Variant ID");

    private final ComboBox<Item> itemField = new ComboBox<>("Item");

    private final TextField specificationField = new TextField("Specification");

    private final ComboBox<String> activeField = new ComboBox<>("Active");

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPage=1;

    private final Span pageInfo = new Span();
    private ItemVariant currentFilter = new ItemVariant();
    
    public ItemVariantView( ItemVariantService itemVariantService, ItemService itemService, SecurityService securityService) {

        this.itemVariantService = itemVariantService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Item Variant List");

        Button addButton = new Button("Add Variant");

        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);

        addButton.addClickListener(e -> {
            ItemVariantForm form =
                    new ItemVariantForm(
                            itemVariantService,
                            itemService,
                            securityService);

            form.open();
        });

        addButton.setVisible(securityService.canAccessView("item-variant-form"));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addButton);

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        variantIdField.setPattern("[0-9]{0,20}");
        variantIdField.setErrorMessage("Enter a valid numbers");

        itemField.setItems(itemService.getItems());
        itemField.setItemLabelGenerator(Item::getItemName);
        itemField.setClearButtonVisible(true);

        activeField.setItems("Yes", "No");
        currentFilter.setActive(null);


        Button searchButton = new Button("Search", e -> applyFilter());

        Button clearButton = new Button("Clear", e -> clearFilter());

        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout filterLayout =
                new HorizontalLayout( variantIdField, itemField, specificationField, activeField, searchButton, clearButton);

        filterLayout.setAlignItems(Alignment.END);
        filterLayout.setWidthFull();

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

        itemVariantGrid.addComponentColumn(variant -> {
            Span badge = new Span(Boolean.TRUE.equals(variant.getActive()) ? "Yes" : "No");
             badge.getStyle()
                 .set("padding", "2px 8px")
                 .set("border-radius", "4px")
                 .set("font-weight", "bold")
                 .set("font-size", "12px");
            if (Boolean.TRUE.equals(variant.getActive())) {
                badge.getStyle().set("background-color", "#dcfce7").set("color", "#15803d");
            } else {
                badge.getStyle().set("background-color", "#fee2e2").set("color", "#b91c1c");
            }
            return badge;
        }).setHeader("Active").setAutoWidth(true);

        itemVariantGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        itemVariantGrid.setSizeFull();

        itemVariantGrid.getStyle().set("border-radius", "12px").set("overflow", "hidden");

        itemVariantGrid.addItemDoubleClickListener(event -> {

            ItemVariant variant = event.getItem();

            getUI().ifPresent(ui -> ui.navigate("item-variant-details/"
                                    + variant.getId()));
        });

        ComboBox<Integer> pageSizeField = new ComboBox<>();

        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadVariants();
        });

        Button previousButton =
                new Button("Previous", e -> {

                    if (currentPage > 0) {
                        currentPage--;
                        loadVariants();
                    }
                });

        Button nextButton =new Button("Next", e -> {
                  if(currentPage<totalPage-1){
                    currentPage++;
                    loadVariants();
                  }
                });
        previousButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout paginationLayout = new HorizontalLayout( previousButton, pageInfo, nextButton,new Span("Page Size"), pageSizeField);

        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        loadVariants();

        add(headerLayout, filterLayout, itemVariantGrid, paginationLayout);

        expand(itemVariantGrid);
    }

    private void loadVariants() {

        Page<ItemVariant> page = itemVariantService.getAllItemVariants( currentFilter, currentPage, pageSize);
        itemVariantGrid.setItems(page.getContent());
        totalPage=page.getTotalPages();
        pageInfo.setText("Page "+ (currentPage + 1)+ " of "+ page.getTotalPages());
    }

    private void applyFilter() {

        Long variantId = null;

        if (!variantIdField.getValue().isEmpty()) {

            variantId = Long.valueOf(variantIdField.getValue().trim());
        }

        currentFilter = new ItemVariant();

        currentFilter.setId(variantId);

        currentFilter.setItem(itemField.getValue());

        currentFilter.setSpecification(specificationField.getValue());

        currentFilter.setActive(activeField.getValue()==null? null : activeField.getValue().equals("YES"));

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