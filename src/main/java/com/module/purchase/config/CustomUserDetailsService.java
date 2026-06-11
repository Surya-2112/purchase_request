package com.module.purchase.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.Users;
import com.module.purchase.repository.UsersRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository userRepository;

    public CustomUserDetailsService(UsersRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException {

        Users user = userRepository.findByUserNameOrUserEmail( username, username).orElseThrow(() ->
                                new UsernameNotFoundException( "User not found"));

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

    private Collection<SimpleGrantedAuthority> getAuthorities(Users user) {

        return Collections.singleton(new SimpleGrantedAuthority(  "ROLE_USER"));
    }
}