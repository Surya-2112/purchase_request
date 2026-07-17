package com.module.purchase.view.assigningConfig;

import org.springframework.data.domain.Page;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entityDTO.AssigningConfigDTO;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.AssigningConfigService;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "assigning-config", layout = MainLayout.class)
@PermitAll
public class AssigningConfigView extends VerticalLayout {

    private final AssigningConfigService assigningConfigService;

    private final Grid<AssigningConfigDTO> assigningConfigGrid = new Grid<>(AssigningConfigDTO.class, false);

    private final TextField idField = new TextField("Config ID");

    private final ComboBox<ApprovalType> approvalTypeField =new ComboBox<>("Approval Type");

    private final IntegerField levelField =new IntegerField("Level");

    private final ComboBox<EmployeeGroup> employeeGroupField = new ComboBox<>("Role Group");

    private int currentPage = 0;
    private int pageSize = 25;
    private int totalPage = 1;

    private final Span pageInfo = new Span();

    private AssigningConfigDTO currentFilter = new AssigningConfigDTO();

    public AssigningConfigView( AssigningConfigService assigningConfigService, SecurityService securityService) {

        this.assigningConfigService = assigningConfigService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        idField.setPattern("[0-9]{0,20}");
        idField.setErrorMessage("Enter vaild Id");

        approvalTypeField.setItems(ApprovalType.values());

        employeeGroupField.setItems(EmployeeGroup.getApprovalGroups());
        employeeGroupField.setItemLabelGenerator(EmployeeGroup::getDisplayName);

        H2 title = new H2("Assigning Config List");

        Button addButton =new Button("Add or Update Assigning Config");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);

        addButton.addClickListener(event -> {

                 getUI().ifPresent(ui -> ui.navigate("assigning-config-form"));
        });

        addButton.setVisible(securityService.canAccessView("assigning-config-form"));

        HorizontalLayout headerLayout =  new HorizontalLayout( title, addButton);

        headerLayout.setWidthFull();

        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        headerLayout.setAlignItems(Alignment.CENTER);

        Button searchButton = new Button("Search",  event -> applyFilter());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button clearButton =new Button( "Clear",event -> clearFilter());
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout filterLayout = new HorizontalLayout(idField, approvalTypeField, levelField,  employeeGroupField, searchButton,clearButton);

        filterLayout.setWidthFull();
        filterLayout.setAlignItems( Alignment.END);

        assigningConfigGrid.addColumn( AssigningConfigDTO::getId) .setHeader("Config ID").setAutoWidth(true);

        assigningConfigGrid.addColumn(config ->
                config.getApprovalType() == null
                        ? ""
                        : config.getApprovalType().name())
                .setHeader("Approval Type")
                .setAutoWidth(true);

        assigningConfigGrid.addColumn( AssigningConfigDTO::getLevel).setHeader("Level")
                .setAutoWidth(true);

        assigningConfigGrid.addColumn(config ->
                config.getEmployeeGroup() == null
                        ? ""
                        : config.getEmployeeGroup().name())
                .setHeader("Role Group").setAutoWidth(true);
                
        assigningConfigGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        
        assigningConfigGrid.setSizeFull();
        assigningConfigGrid.getStyle().set("border-radius", "12px").set("overflow", "hidden");

        assigningConfigGrid.addItemDoubleClickListener(
                event -> {

                    AssigningConfigDTO config = event.getItem();
                    getUI().ifPresent(ui -> ui.navigate("assigning-config-details/"+ config.getId()));
                });

        ComboBox<Integer> pageSizeField = new ComboBox<>();

        pageSizeField.setItems(10, 25, 50, 100);

        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(event -> {

            pageSize = event.getValue();

            currentPage = 0;

            loadAssigningConfigs();
        });

        Button previousButton = new Button("Previous",event -> {
                            if (currentPage > 0) {
                                currentPage--;
                                loadAssigningConfigs();
                            }
                        });

        previousButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button nextButton = new Button("Next",
                        event -> {
                           if(currentPage< totalPage-1){
                            currentPage++;
                            loadAssigningConfigs();
                           }
                        });

        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout paginationLayout = new HorizontalLayout( previousButton,  pageInfo, nextButton,  new Span("Page Size"), pageSizeField);

        paginationLayout.setWidthFull();

        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        paginationLayout.setAlignItems(Alignment.CENTER);

        loadAssigningConfigs();
        add(headerLayout, filterLayout, assigningConfigGrid, paginationLayout);
        expand(assigningConfigGrid);
    }

    private void loadAssigningConfigs() {
        Page<AssigningConfigDTO> page = assigningConfigService.getAllAssigningConfigs( currentFilter, currentPage, pageSize);
        assigningConfigGrid.setItems(page.getContent());
        pageInfo.setText("Page "+ (currentPage + 1) + " of " + page.getTotalPages());
        totalPage=page.getTotalPages();
    }

    private void applyFilter() {

        Long id = null;
        if (!idField.getValue().isBlank()) {
            id = Long.valueOf(idField.getValue().trim());
        }

        currentFilter = new AssigningConfigDTO();
        currentFilter.setId(id);
        currentFilter.setApprovalType(approvalTypeField.getValue());
        currentFilter.setLevel(levelField.getValue());
        currentFilter.setEmployeeGroup( employeeGroupField.getValue());

        currentPage = 0;
        loadAssigningConfigs();
    }

    private void clearFilter() {

        idField.clear();
        approvalTypeField.clear();
        levelField.clear();
        employeeGroupField.clear();

        currentFilter =new AssigningConfigDTO();
        currentPage = 0;
        loadAssigningConfigs();
    }
}