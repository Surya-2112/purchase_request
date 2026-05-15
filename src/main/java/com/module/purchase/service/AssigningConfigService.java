package com.module.purchase.service;

import com.module.purchase.repository.AssigningConfigRepository;
import com.module.purchase.specification.AssigningConfigSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entityDTO.AssigningConfigDTO;
import com.module.purchase.mapper.AssigningConfigMapper;


import java.util.Optional;
import java.util.List;

@Service
public class AssigningConfigService {
    
    @Autowired
    private AssigningConfigRepository assigningConfigRepository;

    @Autowired
    private AssigningConfigMapper assigningConfigMapper;

    public AssigningConfig saveAssigningConfig(AssigningConfig assigningConfig) {
        return assigningConfigRepository.save(assigningConfig);
    }

    public AssigningConfig addAssigningConfig(AssigningConfig assigningConfig) {
        //TODO :need to update
        return saveAssigningConfig(assigningConfig);
    }
    
    public Optional<AssigningConfig> getAssigningConfigById(Long id) {
       Optional<AssigningConfig> existingAssigningConfig = assigningConfigRepository.findById(id);
        if (!existingAssigningConfig.isPresent()) {
                throw new RuntimeException("Assigning config not found with id: " + id);
        }
       return existingAssigningConfig;
    }

    public List<AssigningConfig> getAssigningConfigs() {
        return assigningConfigRepository.findAll();
    }

    public Page<AssigningConfigDTO>  getAllAssigningConfigs(AssigningConfigDTO assigningConfigDTO, int page, int size)
    {
         Specification<AssigningConfig> spec= Specification
        .where(AssigningConfigSpecification.hasId(assigningConfigDTO.getId()))
        .and(AssigningConfigSpecification.hasApprovalType(assigningConfigDTO.getApprovalType()))
        .and(AssigningConfigSpecification.hasEmployeeGroup(assigningConfigDTO.getEmployeeGroup()))
        .and(AssigningConfigSpecification.hasLevel(assigningConfigDTO.getLevel()));

        Pageable pageable= PageRequest.of(page, size);
        Page<AssigningConfig> assignConfigPage = assigningConfigRepository.findAll(spec,pageable);
        return assignConfigPage.map(assigningConfigMapper::toAssigningConfig);
    }

    public void deleteAssigningConfigById(Long Id)
    {
        //TODO: need to added
        assigningConfigRepository.deleteById(Id);
    }

    public AssigningConfig updateAssigningConfig(AssigningConfig assigningConfig)
    {
         //TODO :need to update
        return saveAssigningConfig(assigningConfig);
    }


}
