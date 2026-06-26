package com.module.purchase.view.departmentBudget;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Department;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.enums.ViewName;
import com.module.purchase.service.DepartmentBudgetService;
import com.module.purchase.service.DepartmentService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "department-budget-edit", layout = MainLayout.class)
@PermitAll
public class DepartmentBudgetEditView extends VerticalLayout implements HasUrlParameter<String> {

        private final DepartmentBudgetService departmentBudgetService;
        private final SecurityService securityService;

        private final ComboBox<Department> departmentField = new ComboBox<>("Department");

        private final NumberField totalBudgetAmountField = new NumberField("Total Budget Amount");

        private final NumberField remainingBudgetAmountField = new NumberField("Remaining Budget Amount");

        private final ComboBox<Year> yearField = new ComboBox<>("Year");

        private final NumberField budgetAdjust = new NumberField("Budget Adjustment"); 

        private final Button addButton = new  Button("Upward Adjustments");

        private final Button reduceButton = new Button("Downward Adjustments");

        private DepartmentBudget departmentBudget;

        public DepartmentBudgetEditView(SecurityService securityService, DepartmentBudgetService departmentBudgetService,  DepartmentService departmentService) {

                this.departmentBudgetService = departmentBudgetService;
                this.securityService = securityService;

                setSizeFull();

                setPadding(true);

                List<Department> departments = departmentService.getDepartments();

                departmentField.setItems(departments);

                budgetAdjust.setValue(0.0);

                departmentField.setItemLabelGenerator( Department::getDepartmentName);

                departmentField.setReadOnly(true);

                totalBudgetAmountField.setReadOnly(true);

                remainingBudgetAmountField.setReadOnly(true);

                List<Year> years = new ArrayList<>();
                for (int year = 2000; year <= 2100; year++) {
                        years.add(Year.of(year));
                }
                yearField.setItems(years);
                yearField.setItemLabelGenerator( year -> String.valueOf(year.getValue()));
                yearField.setReadOnly(true);
        }

        @Override
        public void setParameter(BeforeEvent event, String departmentBudgetId) {

                removeAll();
                try{
                departmentBudget = departmentBudgetService.getDepartmentBudgetById(Long.valueOf(departmentBudgetId)).orElse(null);

                if (departmentBudget == null) {
                        add(new H2("Department Budget Not Found"));
                        return;
                }

                H2 title = new H2("Update Department Budget");

                departmentField.setValue( departmentBudget.getDepartment());

                totalBudgetAmountField.setValue(departmentBudget.getTotalBudgetAmount());

                remainingBudgetAmountField.setValue(departmentBudget.getRemainingBudgetAmount());

                yearField.setValue(departmentBudget.getYear());

                FormLayout formLayout = new FormLayout();

                addButton.addClickListener(clickEvent -> addAmount());
                reduceButton.addClickListener(clickEvent-> reduceAmount());

                formLayout.add(departmentField, totalBudgetAmountField, remainingBudgetAmountField, yearField, budgetAdjust,new HorizontalLayout(addButton,reduceButton));

                formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

                Button saveButton = new Button("Save");

                saveButton.addClickListener(clickEvent -> {

                        try {
                                if (departmentField.isEmpty() || totalBudgetAmountField.isEmpty() || remainingBudgetAmountField.isEmpty() || yearField.isEmpty()) {
                                        Notification.show("Please fill all required fields",3000, Notification.Position.TOP_CENTER);
                                        return;
                                }

                                if (totalBudgetAmountField.getValue() < 0) {
                                        totalBudgetAmountField.setEnabled(true);
                                        totalBudgetAmountField.setErrorMessage("Total budgets must be greater then 0");
                                }

                                if (remainingBudgetAmountField.getValue() < 0 || totalBudgetAmountField.getValue() >= remainingBudgetAmountField.getValue()) {
                                        remainingBudgetAmountField.setEnabled(true);
                                        remainingBudgetAmountField.setErrorMessage(
                                                        "Remaining amount must be postive and smaller then Total amount");
                                }

                                departmentBudget.setDepartment(departmentField.getValue());
                                departmentBudget.setTotalBudgetAmount(
                                totalBudgetAmountField.getValue());
                                departmentBudget.setRemainingBudgetAmount(remainingBudgetAmountField.getValue());
                                departmentBudget.setYear(yearField.getValue());
                                departmentBudgetService.updateDepartmentBudget(departmentBudget, securityService.getLoggedInUser().getEmployee());

                                Notification.show("Department Budget Updated Successfully", 3000, Notification.Position.TOP_CENTER);

                                getUI().ifPresent(ui -> ui.navigate("department-budget-details/"+ departmentBudget.getDepartmentBudgetId()));

                        } catch (Exception exception) {
                                Notification.show( exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
                        }

                });

                Button cancelButton = new Button("Cancel");

                cancelButton.addClickListener(clickEvent -> {
                        getUI().ifPresent(ui -> ui.navigate("department-budget-details/"+ departmentBudget.getDepartmentBudgetId()));
                });

                HorizontalLayout buttonLayout = new HorizontalLayout( saveButton, cancelButton);
                add(title, formLayout, buttonLayout);
                
         }catch (NumberFormatException e) {
            event.forwardTo(ViewName.DEPARTMENT_BUDGET.getRoute());
            event.getUI().access(() -> {
                Notification.show("url is not valid ," + e.getMessage(), 3000,
                        Notification.Position.TOP_CENTER);
            });
            return;
        }catch(Exception ex){ 
                event.forwardTo(ViewName.DEPARTMENT_BUDGET.getRoute());
                event.getUI().access(() -> {Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);});
                return;
        }
}
        public void addAmount()
        {
                totalBudgetAmountField.setValue(totalBudgetAmountField.getValue()+(budgetAdjust.getValue()==null?0:budgetAdjust.getValue()));
                remainingBudgetAmountField.setValue(remainingBudgetAmountField.getValue()+(budgetAdjust.getValue()==null?0:budgetAdjust.getValue()));
        }

        public void reduceAmount()
        {
                totalBudgetAmountField.setValue(totalBudgetAmountField.getValue()-(budgetAdjust.getValue()==null?0:budgetAdjust.getValue()));
                remainingBudgetAmountField.setValue(remainingBudgetAmountField.getValue()-(budgetAdjust.getValue()==null?0:budgetAdjust.getValue()));
        }

}