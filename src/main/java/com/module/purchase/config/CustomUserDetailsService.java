package com.module.purchase.config;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.Users;
import com.module.purchase.repository.UserRepository;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        Users user =
                userRepository
                        .findByUserNameOrUserEmail(
                                username,
                                username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found"));

        return new User(

                user.getUserName(),

                user.getPassword(),

                user.getActive(),

                true,

                true,

                true,

                getAuthorities(user)
        );
    }

    private Collection<SimpleGrantedAuthority>
            getAuthorities(Users user) {

        if (user.getEmployee() == null
                || user.getEmployee().getRole() == null
                || user.getEmployee()
                        .getRole()
                        .getEmployeeGroups() == null) {

            return Collections.singleton(
                    new SimpleGrantedAuthority(
                            "ROLE_USER"));
        }

        return user.getEmployee()
                .getRole()
                .getEmployeeGroups()
                .stream()
                .map(group ->
                        new SimpleGrantedAuthority(
                                "ROLE_" + group.name()))
                .collect(Collectors.toList());
    }
}