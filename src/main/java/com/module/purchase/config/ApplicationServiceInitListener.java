package com.module.purchase.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    @Autowired
    private GlobalViewSecurityGuard globalViewSecurityGuard;

    @Override
    public void serviceInit(ServiceInitEvent event) {

        event.getSource()
                .addUIInitListener(uiEvent -> {
                    uiEvent.getUI() .addBeforeEnterListener(globalViewSecurityGuard);
                });
    }
}
