package com.module.purchase.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import com.module.purchase.entity.Users;
import com.module.purchase.repository.UserRepository;

@Service
public class SecurityService {

        private final UserRepository userRepository;

        public SecurityService(UserRepository userRepository) {

                this.userRepository = userRepository;
        }

        public Users getLoggedInUser() {
                String username = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();
                                
                return userRepository.findByUserNameOrUserEmail(username, username)
                                .orElseThrow();
        }

       
}
