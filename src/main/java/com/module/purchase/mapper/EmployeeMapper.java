package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.module.purchase.entity.Employee;
import com.module.purchase.entityDTO.EmployeeDTO;

@Mapper(componentModel="spring")
public interface EmployeeMapper {

    EmployeeDTO toEmployee(Employee employee);
    
    List<EmployeeDTO> toEmployeeList(List<Employee> employees);
}
