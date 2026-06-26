package com.module.purchase.view.department;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entityDTO.DepartmentDTO;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.service.EmployeeService;
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

@Route(value = "department", layout = MainLayout.class)
@PermitAll
public class DepartmentView extends VerticalLayout {

    private DepartmentService departmentService;

    private final Grid<DepartmentDTO> departmentGrid = new Grid<>(DepartmentDTO.class, false);

    private final TextField departmentIdField = new TextField("Department ID");
    private final TextField departmentNameField = new TextField("Department Name");
    private final TextField departmentCodeField = new TextField("Department Code");
    private final ComboBox<String> activeField = new ComboBox<>("Active");
    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPage = 1;

    Span pageInfo = new Span();

    private DepartmentDTO currentFilter = new DepartmentDTO();

    public DepartmentView(DepartmentService departmentService, EmployeeService employeeService,SecurityService securityService) {
        this.departmentService = departmentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        departmentIdField.setErrorMessage("Enter a valid number");
        departmentIdField.setPattern("[0-9]{0,9}");

        activeField.setItems("Yes", "No");

        Button previousButton = new Button("Previous");

        Button nextButton = new Button("Next");

        ComboBox<Integer> pageSizeField = new ComboBox<>();

        pageSizeField.setItems(10, 25, 50, 100);

        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(event -> {

            pageSize = event.getValue();

            currentPage = 0;

            loadDepartments();
        });

        previousButton.addClickListener(event -> {

            if (currentPage > 0) {
                currentPage--;
                loadDepartments();
            }
        });

        nextButton.addClickListener(event -> {
            
        if(currentPage<totalPage-1){
            currentPage++;
            loadDepartments();
        }
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

        HorizontalLayout headerLayout = new HorizontalLayout();

        H2 title = new H2("Deparment List");

        Button addButton = new Button("Add Department");

        addButton.addClickListener(event -> {
            DepartmentForm form = new DepartmentForm(departmentService, employeeService,securityService);
            form.open();
        });

        addButton.setVisible(securityService.canAccessView("department-form"));

        headerLayout.add(
                title,
                addButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);

        HorizontalLayout filterLayout = new HorizontalLayout();

        Button searchButton = new Button(
                "Search",
                event -> applyFilter());

        Button clearButton = new Button(
                "Clear",
                event -> clearFilter());

        filterLayout.setAlignItems(Alignment.END);

        filterLayout.add(
                departmentIdField,
                departmentNameField,
                departmentCodeField,
                activeField,
                searchButton,
                clearButton);

        filterLayout.setWidthFull();

        departmentGrid.addColumn(DepartmentDTO::getDepartmentId).setHeader("Department ID").setAutoWidth(true);

        departmentGrid.addColumn(department -> {
            return department.getDepartmentName() == null? "" : department.getDepartmentName();
        }).setHeader("Department Name").setAutoWidth(true);

        departmentGrid.addColumn(department -> {
            return department.getDepartmentCode() == null  ? "": department.getDepartmentCode();
        }).setHeader("Department Code").setAutoWidth(true);

        departmentGrid.addColumn(department -> {
            return Boolean.TRUE.equals( department.getActive()) ? "Yes": "No";
        }).setHeader("Active").setAutoWidth(true);

        departmentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        departmentGrid.setSizeFull();

        departmentGrid.addItemDoubleClickListener(event -> {
                DepartmentDTO department = event.getItem();
                getUI().ifPresent(ui -> ui.navigate( "department-details/"+ department.getDepartmentId()));
        });

        loadDepartments();

        add(headerLayout,filterLayout,departmentGrid,paginationLayout);

        expand(departmentGrid);

    }

    private void loadDepartments() {
        Page<DepartmentDTO> departmentPage = departmentService.getAllDepartments(currentFilter, currentPage, pageSize);

        departmentGrid.setItems(departmentPage.getContent());
        totalPage=departmentPage.getTotalPages ();
        pageInfo.setText("Page " + (currentPage + 1)+ " of " + departmentPage.getTotalPages());
    }

    private void applyFilter() {

        Long departmentId = null;

        if (!departmentIdField.getValue().isEmpty()) {

            departmentId = Long.valueOf(
                    departmentIdField.getValue().trim());
        }

        currentFilter = new DepartmentDTO();

        currentFilter.setDepartmentId(departmentId);

        currentFilter.setDepartmentName(departmentNameField.getValue());
        currentFilter.setDepartmentCode(departmentCodeField.getValue());

        currentFilter.setActive(
                activeField.getValue() == null
                        ? null
                        : activeField.getValue().equals("Yes"));

        currentPage = 0;

        loadDepartments();

    }

    public void clearFilter() {

        departmentIdField.clear();
        departmentNameField.clear();
        departmentCodeField.clear();
        activeField.clear();

        currentFilter = new DepartmentDTO();
        currentPage = 0;

        loadDepartments();
    }
}
