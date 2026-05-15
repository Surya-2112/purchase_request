package com.module.purchase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import com.module.purchase.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http)
            throws Exception {

        // Configure logout FIRST
        // http.logout(logout -> logout

        //         .logoutUrl("/logout")

        //         .logoutSuccessUrl("/login")

        //         .invalidateHttpSession(true)

        //         .clearAuthentication(true)

        //         .deleteCookies("JSESSIONID")
        // );

        // Vaadin login page
        setLoginView(http, LoginView.class);

        // MUST BE LAST
        super.configure(http);
    }
}