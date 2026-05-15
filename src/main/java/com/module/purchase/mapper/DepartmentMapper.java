package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.module.purchase.entity.Department;
import com.module.purchase.entityDTO.DepartmentDTO;

@Mapper(componentModel="spring")
public interface DepartmentMapper {
    
    DepartmentDTO toDepartmentDTO(Department department);
    
    List<DepartmentDTO> toDepartmentDTOList(List<Department> departments);
}
