package com.module.purchase.view.assigningConfig;

import org.springframework.data.domain.Page;

import com.module.purchase.entityDTO.AssigningConfigDTO;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.AssigningConfigService;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "assigning-config", layout = MainLayout.class)
@PermitAll
public class AssigningConfigView extends VerticalLayout {

    private final AssigningConfigService assigningConfigService;

    private final Grid<AssigningConfigDTO> assigningConfigGrid =
            new Grid<>(AssigningConfigDTO.class, false);

    // FILTERS
    private final TextField idField =
            new TextField("Config ID");

    private final ComboBox<ApprovalType> approvalTypeField =
            new ComboBox<>("Approval Type");

    private final IntegerField levelField =
            new IntegerField("Level");

    private final ComboBox<EmployeeGroup> employeeGroupField =
            new ComboBox<>("Employee Group");

    // PAGINATION
    private int currentPage = 0;

    private int pageSize = 25;

    private final Span pageInfo = new Span();

    private AssigningConfigDTO currentFilter = new AssigningConfigDTO();

    public AssigningConfigView(
            AssigningConfigService assigningConfigService) {

        this.assigningConfigService = assigningConfigService;

        setSizeFull();

        setPadding(true);

        setSpacing(true);

        // LOAD ENUMS
        approvalTypeField.setItems(ApprovalType.values());

        employeeGroupField.setItems(EmployeeGroup.values());

        // HEADER
        HorizontalLayout headerLayout = new HorizontalLayout();

        H2 title =new H2("Assigning Config List");

        Button addButton = new Button("Add Assigning Config");

        addButton.addClickListener(event -> {

            AssigningConfigForm form =new AssigningConfigForm(assigningConfigService);

            form.open();
        });

        headerLayout.add(title,addButton);

        headerLayout.setWidthFull();

        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        headerLayout.setAlignItems( Alignment.CENTER);

        // FILTER
        HorizontalLayout filterLayout =
                new HorizontalLayout();

        Button searchButton =new Button(
                        "Search",
                        event -> applyFilter());

        Button clearButton =new Button(
                        "Clear",
                        event -> clearFilter());

        filterLayout.add(
                idField,
                approvalTypeField,
                levelField,
                employeeGroupField,
                searchButton,
                clearButton);

        filterLayout.setAlignItems(Alignment.END);

        filterLayout.setWidthFull();

        // GRID
        assigningConfigGrid.addComponentColumn(config -> {

            Button idButton =
                    new Button(
                            String.valueOf(config.getId()));

            idButton.addClickListener(event -> {

                getUI().ifPresent(ui ->ui.navigate("assigning-config-details/"+ config.getId()));
            });

            return idButton;

        }).setHeader("Config ID")
                .setAutoWidth(true);

        assigningConfigGrid.addColumn(
                config -> config.getApprovalType() == null
                        ? ""
                        : config.getApprovalType().name())
                .setHeader("Approval Type")
                .setAutoWidth(true);

        assigningConfigGrid.addColumn(
                AssigningConfigDTO::getLevel)
                .setHeader("Level")
                .setAutoWidth(true);

        assigningConfigGrid.addColumn(
                config -> config.getEmployeeGroup() == null
                        ? ""
                        : config.getEmployeeGroup().name())
                .setHeader("Employee Group")
                .setAutoWidth(true);

        assigningConfigGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES);

        assigningConfigGrid.setSizeFull();

        assigningConfigGrid.addItemClickListener(event -> {

            AssigningConfigDTO config =
                    event.getItem();

            Notification.show(
                    "Approval Type : "
                            + config.getApprovalType(),
                    3000,
                    Notification.Position.TOP_CENTER);
        });

        // PAGINATION
        Button previousButton = new Button("Previous");

        Button nextButton = new Button("Next");

        ComboBox<Integer> pageSizeField = new ComboBox<>();
        pageSizeField.setItems(10, 25, 50, 100);
        pageSizeField.setValue(25);

        pageSizeField.addValueChangeListener(e -> {
            pageSize = e.getValue();
            currentPage = 0;
            loadAssigningConfigs();
        });


        previousButton.addClickListener(event -> {

            if (currentPage > 0) {

                currentPage--;

                loadAssigningConfigs();
            }
        });

        nextButton.addClickListener(event -> {

            currentPage++;

            loadAssigningConfigs();
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

        // LOAD DATA
        loadAssigningConfigs();

        add(
                headerLayout,
                filterLayout,
                assigningConfigGrid,
                paginationLayout);

        expand(assigningConfigGrid);
    }

    private void loadAssigningConfigs() {

        Page<AssigningConfigDTO> assigningConfigPage =
                assigningConfigService.getAllAssigningConfigs(
                        currentFilter,
                        currentPage,
                        pageSize);

        assigningConfigGrid.setItems(
                assigningConfigPage.getContent());

        pageInfo.setText(
                "Page "
                        + (currentPage + 1)
                        + " of "
                        + assigningConfigPage.getTotalPages());
    }

    private void applyFilter() {

        Long id = null;

        if (!idField.getValue().isEmpty()) {

            id = Long.valueOf(
                    idField.getValue().trim());
        }

        currentFilter =
                new AssigningConfigDTO();

        currentFilter.setId(id);

        currentFilter.setApprovalType(
                approvalTypeField.getValue());

        currentFilter.setLevel(
                levelField.getValue());

        currentFilter.setEmployeeGroup(
                employeeGroupField.getValue());

        currentPage = 0;

        loadAssigningConfigs();
    }

    private void clearFilter() {

        idField.clear();

        approvalTypeField.clear();

        levelField.clear();

        employeeGroupField.clear();

        currentFilter =
                new AssigningConfigDTO();

        currentPage = 0;

        loadAssigningConfigs();
    }
}