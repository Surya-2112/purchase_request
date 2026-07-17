package com.module.purchase.view.assigningConfig;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.service.AssigningConfigService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(value = "assigning-config-form", layout = MainLayout.class) 
@PermitAll
public class AssigningConfigForm extends VerticalLayout {

    private final AssigningConfigService assigningConfigService;
    private final SecurityService securityService;

    private final ComboBox<ApprovalType> approvalTypeSelector = new ComboBox<>("Select Approval Type");
    private final Grid<AssigningConfig> configGrid = new Grid<>(AssigningConfig.class, false);
    private final Button addLevelButton = new Button("Add New Level");

    private final List<AssigningConfig> currentConfigs = new ArrayList<>();
    private final ListDataProvider<AssigningConfig> dataProvider = new ListDataProvider<>(currentConfigs);
    private final Editor<AssigningConfig> editor = configGrid.getEditor();

    private final IntegerField levelEditField = new IntegerField();
    private final ComboBox<EmployeeGroup> groupEditField = new ComboBox<>();
    private final NumberField minAmountEditField = new NumberField();
    private final NumberField maxAmountEditField = new NumberField();
    private final NumberField marginEditField = new NumberField();

    public AssigningConfigForm(AssigningConfigService assigningConfigService, SecurityService securityService) {
        this.assigningConfigService = assigningConfigService;
        this.securityService = securityService;

        
        setSizeFull();
        setSpacing(true);

        approvalTypeSelector.setItems(ApprovalType.values());
        approvalTypeSelector.setRequiredIndicatorVisible(true);
        approvalTypeSelector.setWidth("300px");
        approvalTypeSelector.addValueChangeListener(event -> loadHierarchyForApprovalType(event.getValue()));

        addLevelButton.setIcon(VaadinIcon.PLUS.create());
        addLevelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        addLevelButton.setEnabled(false);
        addLevelButton.addClickListener(e -> openInlineRowForNewLevel());

        HorizontalLayout topBar = new HorizontalLayout(approvalTypeSelector, addLevelButton);
        topBar.setVerticalComponentAlignment(Alignment.END, addLevelButton);
        
        configureGrid();

        add(new H2("Approval Config"),topBar, configGrid);
    }

    private void configureGrid() {
        configGrid.setSizeFull();
        configGrid.setDataProvider(dataProvider);
        editor.setBuffered(true);

        levelEditField.setWidthFull();
        levelEditField.setReadOnly(true);
        groupEditField.setItems(EmployeeGroup.getApprovalGroups());
        groupEditField.setItemLabelGenerator(EmployeeGroup::getDisplayName);
        groupEditField.setWidthFull();
        minAmountEditField.setWidthFull();
        maxAmountEditField.setWidthFull();
        maxAmountEditField.setReadOnly(true);
        marginEditField.setWidthFull();

        configGrid.addColumn(AssigningConfig::getLevel)
                .setHeader("Level")
                .setEditorComponent(levelEditField)
                .setSortable(true);

        configGrid.addColumn(item -> item.getEmployeeGroup() != null ? item.getEmployeeGroup().getDisplayName() : "")
                .setHeader("Role Group")
                .setEditorComponent(groupEditField);

        configGrid.addColumn(AssigningConfig::getMinAmount)
                .setHeader("Minimum Amount")
                .setEditorComponent(minAmountEditField);

        configGrid.addColumn(item -> item.getMaxAmount() == null ? "∞ (Unlimited)" : item.getMaxAmount())
                .setHeader("Maximum Amount")
                .setEditorComponent(maxAmountEditField);

        configGrid.addColumn(AssigningConfig::getMarginDifferencePercentage)
                .setHeader("Margin %")
                .setEditorComponent(marginEditField);

        configGrid.addComponentColumn(config -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            if (editor.isOpen() && editor.getItem().equals(config)) {
                Button save = new Button(VaadinIcon.CHECK.create(), e -> saveInlineRow(config));
                save.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ICON);
                Button cancel = new Button(VaadinIcon.CLOSE.create(), e -> cancelInlineRow());
                cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
                actions.add(save, cancel);
            } else {
                Button edit = new Button(VaadinIcon.EDIT.create(), e -> {
                    if (editor.isOpen()) {
                        editor.cancel();
                    }
                    levelEditField.setValue(config.getLevel());
                    groupEditField.setValue(config.getEmployeeGroup());
                    minAmountEditField.setValue(config.getMinAmount());
                    maxAmountEditField.setValue(config.getMaxAmount());
                    marginEditField.setValue(config.getMarginDifferencePercentage());

                    editor.editItem(config);
                    configGrid.getDataProvider().refreshItem(config);
                });
                edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                Button delete = new Button(VaadinIcon.TRASH.create(), e -> deleteLevel(config));
                delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

                actions.add(edit, delete);
            }
            return actions;
        }).setHeader("Actions").setWidth("150px").setFlexGrow(0);

        configGrid.addThemeVariants( GridVariant.LUMO_ROW_STRIPES);
    }

    private void loadHierarchyForApprovalType(ApprovalType selectedType) {
        if (selectedType == null) {
            currentConfigs.clear();
            dataProvider.refreshAll();
            addLevelButton.setEnabled(false);
            return;
        }
        
        addLevelButton.setEnabled(true);
        currentConfigs.clear();
        currentConfigs.addAll(assigningConfigService.getByApprovalType(selectedType)); 
        currentConfigs.sort(Comparator.comparing(AssigningConfig::getLevel));
        dataProvider.refreshAll();
    }

    private void openInlineRowForNewLevel() {
        if (editor.isOpen()) {
            showNotification("Please save or cancel your current edits first.", NotificationVariant.LUMO_WARNING);
            return;
        }

        AssigningConfig newConfig = new AssigningConfig();
        newConfig.setApprovalType(approvalTypeSelector.getValue());
        
        if (currentConfigs.isEmpty()) {
            newConfig.setLevel(1);
            newConfig.setMinAmount(0.0);
        } else {
            int nextLevel = currentConfigs.stream().mapToInt(AssigningConfig::getLevel).max().orElse(0) + 1;
            newConfig.setLevel(nextLevel);
            double potentialMin = currentConfigs.get(currentConfigs.size() - 1).getMinAmount() + 1000.0;
            newConfig.setMinAmount(potentialMin);
        }

        currentConfigs.add(newConfig);
        dataProvider.refreshAll();

        levelEditField.setReadOnly(true);
        levelEditField.setValue(newConfig.getLevel());
        groupEditField.setValue(null);
        minAmountEditField.setValue(newConfig.getMinAmount());
        maxAmountEditField.setValue(null);
        marginEditField.setValue(0.0);

        editor.editItem(newConfig);
    }

    private void saveInlineRow(AssigningConfig config) {
        if (levelEditField.isEmpty() || groupEditField.isEmpty() || minAmountEditField.isEmpty() || marginEditField.isEmpty()) {
            showNotification("Please fill all required values.", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            config.setLevel(levelEditField.getValue());
            config.setEmployeeGroup(groupEditField.getValue());
            config.setMinAmount(minAmountEditField.getValue());
            config.setMaxAmount(maxAmountEditField.getValue());
            config.setMarginDifferencePercentage(marginEditField.getValue());

            long levelCount = currentConfigs.stream().filter(c -> c.getLevel().equals(config.getLevel())).count();
            if (levelCount > 1) {
                showNotification("Duplicate Error: Level " + config.getLevel() + " already exists.", NotificationVariant.LUMO_ERROR);
                return;
            }

            long groupCount = currentConfigs.stream().filter(c -> c.getEmployeeGroup() == config.getEmployeeGroup()).count();
            if (groupCount > 1) {
                showNotification("Duplicate Error: Employee Group '" + config.getEmployeeGroup().getDisplayName() + "' already exists.", NotificationVariant.LUMO_ERROR);
                return;
            }

            currentConfigs.sort(Comparator.comparing(AssigningConfig::getLevel));

            for (int i = 0; i < currentConfigs.size(); i++) {
                AssigningConfig current = currentConfigs.get(i);

                if (i == 0) {
                    current.setMinAmount(0.0);
                }

                if (i < currentConfigs.size() - 1) {
                    AssigningConfig next = currentConfigs.get(i + 1);
                    if (next.getMinAmount() <= current.getMinAmount()) {
                        showNotification("Hierarchy Range Error: Level " + next.getLevel() + " Min Amount must be higher.", NotificationVariant.LUMO_ERROR);
                        return;
                    }
                    current.setMaxAmount(next.getMinAmount() - 0.01); 
                } else {
                    current.setMaxAmount(null);
                }
            }

            for (AssigningConfig persistentConfig : currentConfigs) {
                if (persistentConfig.getId() == null) {
                    assigningConfigService.addAssigningConfig(persistentConfig, securityService.getLoggedInUser().getEmployee());
                } else {
                    assigningConfigService.updateAssigningConfig(persistentConfig, securityService.getLoggedInUser().getEmployee());
                }
            }

            editor.save();
            showNotification("Configuration updated successfully.", NotificationVariant.LUMO_SUCCESS);
            loadHierarchyForApprovalType(approvalTypeSelector.getValue());

        } catch (Exception e) {
            showNotification("Database Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void cancelInlineRow() {
        AssigningConfig activeItem = editor.getItem();
        editor.cancel();
        
        if (activeItem != null && activeItem.getId() == null) {
            currentConfigs.remove(activeItem);
        }
        dataProvider.refreshAll();
    }

    private void deleteLevel(AssigningConfig config) {
        try {
            if (config.getId() != null) {
                assigningConfigService.deleteAssigningConfigById(config.getId(), securityService.getLoggedInUser().getEmployee());
            }
            currentConfigs.remove(config);
            
            currentConfigs.sort(Comparator.comparing(AssigningConfig::getLevel));
            for (int i = 0; i < currentConfigs.size(); i++) {
                AssigningConfig current = currentConfigs.get(i);
                current.setLevel(i+1);
                if (i == 0) current.setMinAmount(0.0);
                
                if (i < currentConfigs.size() - 1) {
                    current.setMaxAmount(currentConfigs.get(i + 1).getMinAmount() - 0.01);
                } else {
                    current.setMaxAmount(null);
                }
                
                if (current.getId() != null) {
                    assigningConfigService.updateAssigningConfig(current, securityService.getLoggedInUser().getEmployee());
                }
            }

            showNotification("Level deleted and boundaries balanced.", NotificationVariant.LUMO_SUCCESS);
            loadHierarchyForApprovalType(approvalTypeSelector.getValue());
        } catch (Exception e) {
            showNotification("Failed to delete record: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String text, NotificationVariant variant) {
        Notification notification = Notification.show(text, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }
}