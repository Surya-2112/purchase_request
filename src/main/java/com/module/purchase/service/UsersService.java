package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.module.purchase.repository.UsersRepository;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Users;
import com.module.purchase.entityDTO.UsersDTO;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceIsNotActiveException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.mapper.UsersMapper;
import com.module.purchase.specification.UsersSpecification;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersService {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogsService auditLogsService;

    public Users saveUsers(Users users) {

        return userRepository.save(users);
    }

    public Users addUsers(Users user, Employee created) {
        Optional<Users> existingUser = userRepository.findByUserEmail(user.getUserEmail());
        if (existingUser.isPresent()) {
            throw new ResourceAlreadyUsedException("User already exists with email: " + user.getUserEmail());
        }

        Employee employee = employeeService.getEmployeeById(user.getEmployee().getEmployeeId()).get();
        if (employee.getUsers() != null) {
            throw new ResourceAlreadyUsedException("This Employee as another user");
        }

        employeeService.updateEmployee(employee, created);
        if (user.getUserId() == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        user = saveUsers(user);
        employee.setUsers(user);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.USER);
        log.setEntityId(user.getUserId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(created);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return user;
    }

    public Optional<Users> getUserById(Long id) {
        Optional<Users> existingUser = userRepository.findById(id);
        if (!existingUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
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

    public Users updateUser(Users user,Employee employee) {
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

            user.setPassword(
                    existingUser.getPassword());
        }
        user=saveUsers(user);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.USER);
        log.setEntityId(user.getUserId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return user;
    }

    public void deleteUsersById(Long usersId,Employee employee) {
        Users existingUser = getUserById(usersId).get();
        if (existingUser.getEmployee().getActive()) {
            throw new RuntimeException("cannot delete user because employee is active");
        }
        userRepository.deleteById(usersId);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.USER);
        log.setEntityId(usersId);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }
}
