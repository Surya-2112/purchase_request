package com.module.purchase.view.vendor;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entityDTO.VendorDTO;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.VendorService;
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
import java.util.List;

import jakarta.annotation.security.PermitAll;

@Route(value = "vendor", layout = MainLayout.class)
@PermitAll
public class VendorView extends VerticalLayout {

    private final VendorService vendorService;

    private final Grid<VendorDTO> vendorGrid = new Grid<>(VendorDTO.class, false);

    private final TextField vendorIdField = new TextField("Vendor ID");
    private final TextField vendorNameField = new TextField("Vendor Name");

    private final ComboBox<Category> categoryField =
            new ComboBox<>("Category");

    private final ComboBox<String> activeField =
            new ComboBox<>("Active");

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPages = 1;

    private final Span pageInfo = new Span();

    private VendorDTO currentFilter = new VendorDTO();

    public VendorView(
            VendorService vendorService,
            CategoryService categoryService,
            SecurityService securityService) {

        this.vendorService = vendorService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        activeField.setItems("Yes", "No");

        categoryField.setItems(categoryService.getCategories());
        categoryField.setItemLabelGenerator(Category::getCategoryName);
        categoryField.setClearButtonVisible(true);
            
        H2 title = new H2("Vendor List");

        Button addButton = new Button("Add Vendor", e -> {
            VendorForm form = new VendorForm(
                    vendorService,
                    categoryService,
                    securityService
            );
            form.open();
        });

        addButton.setVisible(
                securityService.canAccessView("vendor-form"));

        HorizontalLayout headerLayout =
                new HorizontalLayout(title, addButton);

        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(
                JustifyContentMode.BETWEEN);

        Button searchButton = new Button("Search", e -> applyFilter());
        Button clearButton = new Button("Clear", e -> clearFilter());

        HorizontalLayout filterLayout = new HorizontalLayout(
                vendorIdField,
                vendorNameField,
                categoryField,
                activeField,
                searchButton,
                clearButton
        );

        filterLayout.setWidthFull();
        filterLayout.setAlignItems(Alignment.END);

        vendorGrid.addColumn(VendorDTO::getVendorId)
                .setHeader("Vendor ID")
                .setAutoWidth(true);

        vendorGrid.addColumn(VendorDTO::getVendorName)
                .setHeader("Vendor Name")
                .setAutoWidth(true);

        vendorGrid.addColumn(vendor -> vendor.getCategories() == null
                        ? ""
                        : vendor.getCategories()
                        .stream()
                        .map(Category::getCategoryName)
                        .collect(Collectors.joining(", "))
        ).setHeader("Categories");

        vendorGrid.addColumn(vendor ->
                Boolean.TRUE.equals(vendor.getActive())
                        ? "Yes"
                        : "No"
        ).setHeader("Active");

        vendorGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        vendorGrid.setSizeFull();

        vendorGrid.addItemDoubleClickListener(event -> {
            VendorDTO vendor = event.getItem();
            getUI().ifPresent(ui ->
                    ui.navigate("vendor-details/" + vendor.getVendorId())
            );
        });

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadVendors();
        });

        Button previousButton = new Button("Previous", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadVendors();
            }
        });

        Button nextButton = new Button("Next", e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
               loadVendors();
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
        paginationLayout.setJustifyContentMode(
                JustifyContentMode.CENTER);
        paginationLayout.setAlignItems(Alignment.CENTER);

        loadVendors();

        add(headerLayout, filterLayout, vendorGrid, paginationLayout);
        expand(vendorGrid);
    }

    private void loadVendors() {

        Page<VendorDTO> page = vendorService.getAllVendors(
                currentFilter,
                currentPage,
                pageSize
        );

        vendorGrid.setItems(page.getContent());

        this.totalPages = page.getTotalPages() > 0 ? page.getTotalPages() : 1;

        pageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
    }

    private void applyFilter() {

        Long vendorId = null;

        if (!vendorIdField.getValue().isEmpty()) {
            vendorId = Long.valueOf(vendorIdField.getValue().trim());
        }

        currentFilter = new VendorDTO();
        currentFilter.setVendorId(vendorId);
        currentFilter.setVendorName(vendorNameField.getValue());

       if (categoryField.getValue() != null) {
    currentFilter.setCategories(
            List.of(categoryField.getValue())
    );
} else {
    currentFilter.setCategories(null);
}

        currentFilter.setActive(
                activeField.getValue() == null
                        ? null
                        : activeField.getValue().equals("Yes")
        );

        currentPage = 0;
        loadVendors();
    }

    private void clearFilter() {

        vendorIdField.clear();
        vendorNameField.clear();
        categoryField.clear();
        activeField.clear();

        currentFilter = new VendorDTO();
        currentPage = 0;

        loadVendors();
    }
}