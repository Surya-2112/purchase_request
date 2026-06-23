package com.module.purchase.view.category;

import java.util.Optional;

import com.module.purchase.config.SecurityService;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.service.CategoryService;
import com.module.purchase.service.RepeatedPeriodService;
import com.module.purchase.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@Route(value = "category-details", layout = MainLayout.class)
@PermitAll
public class CategoryDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

        private final CategoryService categoryService;
        private final RepeatedPeriodService repeatedPeriodService;
        private final SecurityService securityService;

        public CategoryDetailsView(CategoryService categoryService, RepeatedPeriodService repeatedPeriodService, 
                                   SecurityService securityService) {
                this.categoryService = categoryService;
                this.repeatedPeriodService = repeatedPeriodService;
                this.securityService = securityService;

                setSizeFull();
                setPadding(true);
                setSpacing(true);
        }

        @Override
        public void setParameter(BeforeEvent event, Long categoryId) {

                removeAll();
                try{
                Category category = categoryService.getCategoryById(categoryId).orElse(null);
                if (category == null) {
                        add(new Span("Category Not Found"));
                        return;
                }

                H2 title = new H2("Category Details");

                FormLayout baseFormLayout = new FormLayout();

                baseFormLayout.addFormItem(new Span(String.valueOf(category.getCategoryId())), "Category ID");

                baseFormLayout.addFormItem(new Span(category.getCategoryName() == null ? "" : category.getCategoryName()), "Category Name");

                baseFormLayout.addFormItem(new Span(category.isRepeatable() ? "Yes" : "No"),  "Is Repeatable Category");

                baseFormLayout.addFormItem(new Span(category.isAutoRfq() ? "Yes" : "No"), "Send RFQ Automatically");

                VerticalLayout scheduleContainer = new VerticalLayout();
                scheduleContainer.setPadding(false);
                scheduleContainer.setSpacing(true);

                if (category.isAutoRfq()) {
                        Optional<RepeatedPeriod> periodOpt = repeatedPeriodService
                                        .findByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, category.getCategoryId());

                        if (periodOpt.isPresent()) {
                                RepeatedPeriod period = periodOpt.get();
                                FormLayout scheduleFormLayout = new FormLayout();

                                String intervalText = "Every " + period.getFrequencyPeriod() + " " + 
                                                      (period.getFrequencyType() != null ? period.getFrequencyType().name().toLowerCase() : "");
                                
                                scheduleFormLayout.addFormItem(new Span(intervalText), "Recurrence Interval");
                                
                                scheduleFormLayout.addFormItem(new Span(period.getFromDate() != null ? period.getFromDate().toString() : "-"), 
                                                "Sourcing Start Date");
                                
                                scheduleFormLayout.addFormItem(new Span(period.getToDate() != null ? period.getToDate().toString() : "Indefinite / No End Date"), 
                                                "Sourcing End Date");
                                
                                scheduleFormLayout.addFormItem(new Span(period.getNextDate() != null ? period.getNextDate().toString() : "Deactivated"), 
                                                "Next Automated RFQ Run");

                                scheduleContainer.add(new H3("Automated RFQ Schedule Profiles"), scheduleFormLayout);
                        }
                }

                Button updateButton = new Button("Update");
                updateButton.addClickListener(e -> getUI()
                                .ifPresent(ui -> ui.navigate("category-edit/" + category.getCategoryId())));

                Button deleteButton = new Button("Delete");
                deleteButton.addClickListener(e -> {
                        ConfirmDialog dialog = new ConfirmDialog();
                        dialog.setHeader("Delete Category");
                        dialog.setText("Are you sure you want to delete this category? All associated automation routines will be unlinked.");
                        dialog.setCancelable(true);
                        dialog.setConfirmText("Delete");
                        dialog.setConfirmButtonTheme("error primary");

                        dialog.addConfirmListener(confirmEvent -> {
                                try {
                                        categoryService.deleteCategoryById(category.getCategoryId(), securityService.getLoggedInUser().getEmployee());
                                        
                                        // Clean up cascade orphans explicitly
                                        repeatedPeriodService.deleteByReferTypeAndReferId(RepeatedPeriodReferType.CATEGORY, category.getCategoryId());

                                        Notification.show("Category and Schedules Purged", 3000, Notification.Position.TOP_CENTER);
                                        getUI().ifPresent(ui -> ui.navigate("category"));
                                } catch (Exception exception) {
                                        Notification.show(exception.getMessage(), 5000, Notification.Position.TOP_CENTER);
                                }
                        });
                        dialog.open();
                });
                
                updateButton.setVisible(securityService.canAccessView("category-edit"));
                deleteButton.setVisible(securityService.canAccessView("category-form"));

                HorizontalLayout buttons = new HorizontalLayout(updateButton, deleteButton);

                add(title, baseFormLayout, scheduleContainer, buttons);
                }catch(Exception ex)
                {      event.forwardTo("category");
                       event.getUI().access(() -> {
                        Notification.show(ex.getMessage(),3000,Notification.Position.TOP_CENTER);
                        });
                                return;
                }

        }
}