package com.module.purchase.view.vendorCategory;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.VendorCategory;
import com.module.purchase.service.VendorCategoryService;
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

@Route(value = "vendor-category", layout = MainLayout.class)
@PermitAll
public class VendorCategoryView extends VerticalLayout {

    private final VendorCategoryService categoryService;

    private final SecurityService securityService;


    private final Grid<VendorCategory> categoryGrid = new Grid<>(VendorCategory.class, false);

    private final TextField categoryIdField = new TextField("Category ID");

    private final TextField categoryNameField = new TextField("Category Name");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private VendorCategory currentFilter = new VendorCategory();

    public VendorCategoryView(VendorCategoryService categoryService , SecurityService securityService) {

        this.categoryService = categoryService;
        this.securityService =securityService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // HEADER
        H2 title = new H2("Vendor Category List");

        Button addButton = new Button("Add Category");
        addButton.addClickListener(event -> {
            // TODO ::
           VendorCategoryForm form = new VendorCategoryForm(categoryService,securityService);
           form.open();
        });

        HorizontalLayout headerLayout =
                new HorizontalLayout(title, addButton);

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // FILTER
        Button searchButton = new Button("Search", e -> applyFilter());
        Button clearButton = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout = new HorizontalLayout(
                categoryIdField,
                categoryNameField,
                searchButton,
                clearButton
        );

        filterLayout.setWidthFull();
        filterLayout.setAlignItems(Alignment.END);

        // GRID
        categoryGrid.addComponentColumn(category -> {

            Button idButton =new Button(String.valueOf(category.getCategoryId()));

            idButton.addClickListener(e -> {
                getUI().ifPresent(ui ->
                        ui.navigate("vendor-category-details/" + category.getCategoryId())
                );
            });

            return idButton;

        }).setHeader("Category ID").setAutoWidth(true);

        categoryGrid.addColumn(VendorCategory::getCategoryName)
                .setHeader("Category Name")
                .setAutoWidth(true);

        categoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        categoryGrid.setSizeFull();

        categoryGrid.addItemClickListener(event -> {
            VendorCategory category = event.getItem();

            Notification.show(
                    "Category: " + category.getCategoryName(),
                    3000,
                    Notification.Position.TOP_CENTER
            );
        });

        // PAGINATION
        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadCategories();
        });

        Button previousButton = new Button("Previous", e -> {
            if (currentPage > 0) { currentPage--;
                loadCategories();
            }
        });

        Button nextButton = new Button("Next", e -> { currentPage++;
            loadCategories();
        });

        HorizontalLayout paginationLayout = new HorizontalLayout(
                previousButton,
                pageInfo,
                nextButton,
                new Span("Page Size"),
                pageSizeField
        );

        paginationLayout.setWidthFull();
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        // LOAD
        loadCategories();

        add(headerLayout, filterLayout, categoryGrid, paginationLayout);
        expand(categoryGrid);
    }

    private void loadCategories() {

        Page<VendorCategory> page = categoryService.getAllVendorCategories(
                currentFilter,
                currentPage,
                pageSize
        );

        categoryGrid.setItems(page.getContent());

        pageInfo.setText("Page " + (currentPage + 1)+ " of " + page.getTotalPages()
        );
    }

    private void applyFilter() {

        Long id = null;

        if (!categoryIdField.getValue().isEmpty()) {
            id = Long.valueOf(categoryIdField.getValue().trim());
        }

        currentFilter = new VendorCategory();
        currentFilter.setCategoryId(id);
        currentFilter.setCategoryName(categoryNameField.getValue());

        currentPage = 0;
        loadCategories();
    }

    private void clearFilter() {

        categoryIdField.clear();
        categoryNameField.clear();

        currentFilter = new VendorCategory();
        currentPage = 0;

        loadCategories();
    }
}