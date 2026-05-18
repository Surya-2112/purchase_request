package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.module.purchase.repository.UserRepository;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entityDTO.UsersDTO;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.PageRequest;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceIsNotActiveException;
import com.module.purchase.mapper.UsersMapper;
import com.module.purchase.specification.UsersSpecification;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users saveUsers(Users users) {

        return userRepository.save(users);
    }

    public Users addUsers(Users user) {
        Optional<Users> existingUser = userRepository.findByUserEmail(user.getUserEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists with email: " + user.getUserEmail());
        }
        Employee employee = employeeService.getEmployeeById(user.getEmployee().getEmployeeId()).get();
        if (employee.getUser() != null) {
            throw new RuntimeException("This Employee as another user");
        }
        employeeService.updateEmployee(employee);
         if (user.getUserId() == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user = saveUsers(user);
        employee.setUser(user);
        
        return user;
    }

    public Optional<Users> getUserById(Long id) {
        Optional<Users> existingUser = userRepository.findById(id);
        if (!existingUser.isPresent()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return existingUser;
    }

    public List<Users> getUsers() {
        return userRepository.findAll();
    }

    public Page<UsersDTO> getAllUsers(UsersDTO usersDTO, int page, int size) {
        Specification<Users> spec = Specification
                .where(UsersSpecification.hasUserId(usersDTO.getUserId()))
                .and(UsersSpecification.hasUserName(usersDTO.getUserName()))
                .and(UsersSpecification.hasuserEmail(usersDTO.getUserEmail()))
                .and(UsersSpecification.hasEmployee(usersDTO.getEmployee()))
                .and(UsersSpecification.hasActive(usersDTO.getActive()));

        PageRequest pageable = PageRequest.of(page, size);

        Page<Users> pageUsers = userRepository.findAll(spec, pageable);

        return pageUsers.map(usersMapper::toUserDTO);
    }

    public Users updateUser(Users user) {
        Users existingUser = getUserById(user.getUserId()).get();

        if (!existingUser.getEmployee().getActive()) {
            throw new ResourceIsNotActiveException("Employee is not active");
        }
        if (!existingUser.getUserEmail().equals(user.getUserEmail())) {
            throw new ModificationNotAllowedException("Cannot update user email ");
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(
                            user.getPassword()));
        } else {

            // Keep old password
            user.setPassword(
                    existingUser.getPassword());
        }
        return saveUsers(user);
    }

    public void deleteUsersById(Long usersId) {
        Users existingUser = getUserById(usersId).get();
        if (existingUser.getEmployee().getActive()) {
            throw new RuntimeException("cannot delete user because employee is active");
        }
        userRepository.deleteById(usersId);
    }
}
