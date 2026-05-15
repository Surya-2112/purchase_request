package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entityDTO.DepartmentBudgetDTO;

@Mapper(componentModel="spring")
public interface  DepartmentBudgetMapper {
    
    DepartmentBudgetDTO toDepartmentBudgetDTO(DepartmentBudget departmentBudget);

    List<DepartmentBudgetDTO> toDepartmentBudgetDTOList(List<DepartmentBudget> departmentBudget);
}
