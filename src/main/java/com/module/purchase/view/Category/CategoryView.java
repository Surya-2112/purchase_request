package com.module.purchase.view.category;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.RepeatedPeriodService;
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

@Route(value = "category", layout = MainLayout.class)
@PermitAll
public class CategoryView extends VerticalLayout {

    private final CategoryService categoryService;

    private final Grid<Category> categoryGrid = new Grid<>(Category.class, false);

    // ================= FILTER FIELDS =================
    private final TextField categoryIdField = new TextField("Category ID");
    private final TextField categoryNameField = new TextField("Category Name");
    private final ComboBox<String> repeatableField = new ComboBox<>("Is Repeatable");
    private final ComboBox<String> autoRfqField = new ComboBox<>("Auto RFQ");

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;

    private final Span pageInfo = new Span();

    // Using a dedicated DTO container class to support true null selections for booleans
    private Category currentFilter = new Category();

    public CategoryView(CategoryService categoryService, SecurityService securityService, RepeatedPeriodService repeatedPeriodService) {

        this.categoryService = categoryService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // INITIALIZE COMBOCBOX OPTIONS WITHOUT BOOTSTRAPPING DUMMY "ALL" VALUES
        repeatableField.setItems("Yes", "No");
        repeatableField.setClearButtonVisible(true);
        repeatableField.setWidth("140px");

        autoRfqField.setItems("Yes", "No");
        autoRfqField.setClearButtonVisible(true);
        autoRfqField.setWidth("140px");

        // HEADER
        H2 title = new H2("Category List");

        Button addButton = new Button("Add Category", event -> {
            CategoryForm form = new CategoryForm(categoryService, repeatedPeriodService, securityService);
            form.addDetachListener(detachEvent -> loadCategories());
            form.open();
        });

        addButton.setVisible(securityService.canAccessView("category-form"));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // FILTER CONTROL BUTTONS
        Button searchButton = new Button("Search", e -> applyFilter());
        Button clearButton = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout = new HorizontalLayout(
                categoryIdField,
                categoryNameField,
                repeatableField,
                autoRfqField,
                searchButton,
                clearButton
        );
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(Alignment.END);

        // GRID CONFIGURATION WITH FIXED WIDTHS FOR BOOLEANS
        categoryGrid.addColumn(Category::getCategoryId).setHeader("Category ID").setWidth("120px").setFlexGrow(0);
        categoryGrid.addColumn(Category::getCategoryName).setHeader("Category Name").setAutoWidth(true);
        
        categoryGrid.addColumn(category -> category.isRepeatable() ? "Yes" : "No")
                .setHeader("Is Repeatable").setWidth("140px").setFlexGrow(0);
                
        categoryGrid.addColumn(category -> category.isAutoRfq() ? "Yes" : "No")
                .setHeader("Auto RFQ").setWidth("140px").setFlexGrow(0);

        categoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        categoryGrid.setSizeFull();

        categoryGrid.addItemDoubleClickListener(event -> {
            Category category = event.getItem();
            getUI().ifPresent(ui -> ui.navigate("category-details/" + category.getCategoryId()));
        });

        // PAGE SIZE SELECTOR
        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                pageSize = e.getValue();
                currentPage = 0;
                loadCategories();
            }
        });

        // PAGINATION CONTROLS
        Button previousButton = new Button("Previous", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadCategories();
            }
        });

        Button nextButton = new Button("Next", e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadCategories();
            }
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

        currentFilter.setAutoRfq(null);
        currentFilter.setRepeatable(null);
        loadCategories();
        System.out.println("dffd");

        add(headerLayout, filterLayout, categoryGrid, paginationLayout);
        expand(categoryGrid);
    }

    private void loadCategories() {
        System.out.println(currentFilter.toString());
        Page<Category> page = categoryService.getAllCategories(
                currentFilter,
                currentPage,
                pageSize
        );

        categoryGrid.setItems(page.getContent());
        this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;

        pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
    }

    private void applyFilter() {
        Long id = null;

        if (!categoryIdField.getValue().isEmpty()) {
            try {
                id = Long.valueOf(categoryIdField.getValue().trim());
            } catch (NumberFormatException e) {
                id = -1L; 
            }
        }

        currentFilter = new Category();

        currentFilter.setCategoryId(id);
        
        // Pass empty inputs down as null references cleanly
        currentFilter.setCategoryName(categoryNameField.isEmpty() ? null : categoryNameField.getValue().trim());

        // Standard clean null evaluations
        currentFilter.setRepeatable(repeatableField.getValue() == null ? null : repeatableField.getValue().equals("Yes"));

        currentFilter.setAutoRfq(
                autoRfqField.getValue() == null ? null : autoRfqField.getValue().equals("Yes")
        );

        currentPage = 0;
        loadCategories();
    }

    private void clearFilter() {
        categoryIdField.clear();
        categoryNameField.clear();
        repeatableField.clear();
        autoRfqField.clear();

        currentFilter = new Category();
        currentFilter.setAutoRfq(null);
        currentFilter.setRepeatable(null);
        currentPage = 0;

        loadCategories();
    }
}