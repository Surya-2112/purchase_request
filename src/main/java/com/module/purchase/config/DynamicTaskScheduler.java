package com.module.purchase.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.module.purchase.service.RepeatedPeriodService;

@Component
public class DynamicTaskScheduler {

    @Autowired
    RepeatedPeriodService repeatedPeriodService;
    
    @Scheduled(cron = "0 0 12 * * *") 
    public void scheduledTask(){
        repeatedPeriodService.assignedRepeatedTask();
    }
}
