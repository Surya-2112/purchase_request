package com.module.purchase.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.Users;
import com.module.purchase.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository usersRepository;

        public CustomUserDetailsService(UserRepository usersRepository) {

                this.usersRepository = usersRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String username)
                        throws UsernameNotFoundException {

                Users user = usersRepository.findByUserNameOrUserEmail(username, username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                String roleName = "USER";

                if (user.getEmployee() != null &&user.getEmployee().getRole() != null) {
                        roleName = user.getEmployee().getRole() .getRoleName();
                }
                return new User(user.getUserName(), user.getPassword(), user.getActive(), true, true, true,
                                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roleName)));
        }
}