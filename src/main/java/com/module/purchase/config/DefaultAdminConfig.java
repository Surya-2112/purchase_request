package com.module.purchase.config;

import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Role;
import com.module.purchase.entity.Users;
import com.module.purchase.enums.EmployeeGroup;
import com.module.purchase.repository.EmployeeRepository;
import com.module.purchase.repository.RoleRepository;
import com.module.purchase.repository.UserRepository;

@Configuration
public class DefaultAdminConfig {

    @Bean
    CommandLineRunner createDefaultAdmin(

            UserRepository userRepository,

            EmployeeRepository employeeRepository,

            RoleRepository roleRepository,

            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.count() == 0) {

                Role role = new Role();

                role.setRoleName("SUPER_ADMIN");

                role.setEmployeeGroups(
                        Arrays.asList(EmployeeGroup.values())
                );

                role = roleRepository.save(role);

                Users user = new Users();

                user.setUserName("admin");

                user.setUserEmail("admin@gmail.com");

                user.setPassword(
                        passwordEncoder.encode("admin123")
                );

                user.setActive(true);

                user = userRepository.save(user);

                Employee employee = new Employee();

                employee.setEmployeeName("Super Admin");

                employee.setEmployeeEmail(
                        "admin@gmail.com"
                );

                employee.setActive(true);

                employee.setRole(role);

                employee.setUser(user);

                employeeRepository.save(employee);
            }
        };
    }
}