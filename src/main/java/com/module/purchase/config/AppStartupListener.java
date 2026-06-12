package com.module.purchase.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.module.purchase.service.RepeatedPeriodService;

@Component
public class AppStartupListener {

    @Autowired
    private RepeatedPeriodService repeatedPeriodService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        
        try {
            repeatedPeriodService.assignedRepeatedTask();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
