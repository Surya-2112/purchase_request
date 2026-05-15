package com.module.purchase.mapper;

import org.mapstruct.Mapper;

import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entityDTO.AssigningConfigDTO;
import java.util.List;

@Mapper(componentModel="spring")
public interface AssigningConfigMapper {
    
    AssigningConfigDTO toAssigningConfig(AssigningConfig assigningConfig);

    List<AssigningConfigDTO> toAssigningConfigList(List<AssigningConfig> assigningConfig);
}
