package com.module.purchase.view.category;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.service.CategoryService;
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

    private final TextField categoryIdField = new TextField("Category ID");

    private final TextField categoryNameField = new TextField("Category Name");

    private int currentPage = 0;
    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private Category currentFilter = new Category();

    public CategoryView(CategoryService categoryService , SecurityService securityService) {

        this.categoryService = categoryService;
 //       this.securityService =securityService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // HEADER
        H2 title = new H2("Category List");

        Button addButton = new Button("Add Category");
        addButton.addClickListener(event -> {
        CategoryForm form = new CategoryForm(categoryService,securityService);
           form.open();
        });

        addButton.setVisible(securityService.canAccessView("category-form"));

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
        categoryGrid.addColumn(Category::getCategoryId).setHeader("Category ID").setAutoWidth(true);

        categoryGrid.addColumn(Category::getCategoryName)
                .setHeader("Category Name")
                .setAutoWidth(true);

        categoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        categoryGrid.setSizeFull();

        categoryGrid.addItemDoubleClickListener(event -> {
                Category category = event.getItem();
                getUI().ifPresent(ui -> ui.navigate("category-details/" + category.getCategoryId()));

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

        Page<Category> page = categoryService.getAllCategories(
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

        currentFilter = new Category();
        currentFilter.setCategoryId(id);
        currentFilter.setCategoryName(categoryNameField.getValue());

        currentPage = 0;
        loadCategories();
    }

    private void clearFilter() {

        categoryIdField.clear();
        categoryNameField.clear();

        currentFilter = new Category();
        currentPage = 0;

        loadCategories();
    }
}