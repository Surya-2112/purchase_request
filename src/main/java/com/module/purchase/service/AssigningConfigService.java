package com.module.purchase.service;

import com.module.purchase.repository.AssigningConfigRepository;
import com.module.purchase.specification.AssigningConfigSpecification;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.AssigningConfig;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entityDTO.AssigningConfigDTO;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;
import com.module.purchase.mapper.AssigningConfigMapper;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;

@Service
@Transactional
public class AssigningConfigService {

    @Autowired
    private AssigningConfigRepository assigningConfigRepository;

    @Autowired
    private AssigningConfigMapper assigningConfigMapper;

    @Autowired
    private AuditLogsService auditLogsService;

    public AssigningConfig saveAssigningConfig(AssigningConfig assigningConfig) {
        return assigningConfigRepository.save(assigningConfig);
    }

    public AssigningConfig addAssigningConfig(AssigningConfig assigningConfig, Employee employee) {

        Optional<AssigningConfig> exist=assigningConfigRepository.findByApprovalTypeAndLevel(assigningConfig.getApprovalType(),assigningConfig.getLevel());
        if(exist.isPresent())
        {
            throw new ResourceAlreadyUsedException("AssigningConfig is already have this level for this approval type");
        }
        
        assigningConfig=saveAssigningConfig(assigningConfig);
        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.ASSIGNING_CONFIG);
        log.setEntityId(assigningConfig.getId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        
        return assigningConfig;
    }

    public List<AssigningConfig> getByApprovalType(ApprovalType approvalType) {
        return assigningConfigRepository.findByApprovalType(approvalType);
    }

    public List<AssigningConfig> getConfigs(ApprovalType approvalType, Double totalAmount) {
        return assigningConfigRepository.findByApprovalTypeAndMinAmountLessThanEqual(approvalType,totalAmount);
    }

    public Optional<AssigningConfig> getAssigningConfigById(Long id) {
        Optional<AssigningConfig> existingAssigningConfig = assigningConfigRepository.findById(id);
        if (!existingAssigningConfig.isPresent()) {
            throw new ResourceNotFoundException("Assigning config not found with id: " + id);
        }
        return existingAssigningConfig;
    }

    public List<AssigningConfig> getAssigningConfigs() {
        return assigningConfigRepository.findAll();
    }

    public Page<AssigningConfigDTO> getAllAssigningConfigs(AssigningConfigDTO assigningConfigDTO, int page, int size) {
        Specification<AssigningConfig> spec = Specification
                .where(AssigningConfigSpecification.hasId(assigningConfigDTO.getId()))
                .and(AssigningConfigSpecification.hasApprovalType(assigningConfigDTO.getApprovalType()))
                .and(AssigningConfigSpecification.hasEmployeeGroup(assigningConfigDTO.getEmployeeGroup()))
                .and(AssigningConfigSpecification.hasLevel(assigningConfigDTO.getLevel()));

        Pageable pageable = PageRequest.of(page, size);
        Page<AssigningConfig> assignConfigPage = assigningConfigRepository.findAll(spec, pageable);
        return assignConfigPage.map(assigningConfigMapper::toAssigningConfig);
    }

    public void deleteAssigningConfigById(Long Id,Employee employee) {

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.ASSIGNING_CONFIG);
        log.setEntityId(Id);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        
        assigningConfigRepository.deleteById(Id);
    }

    public AssigningConfig updateAssigningConfig(AssigningConfig assigningConfig, Employee employee) {
        
        assigningConfig=saveAssigningConfig(assigningConfig);
        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.ASSIGNING_CONFIG);
        log.setEntityId(assigningConfig.getId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        
        return assigningConfig;
    }

public List<AssigningConfig> determineRequiredApprovals( Double chosenQuoteAmount,  Double lowestQuoteAmount, 
        ApprovalType approvalType) {

    double marginDiffPercent = 0.0;
    if (chosenQuoteAmount > lowestQuoteAmount && lowestQuoteAmount > 0) {
        marginDiffPercent = ((chosenQuoteAmount - lowestQuoteAmount) / lowestQuoteAmount) * 100.0;
    }

    List<AssigningConfig> matchingConfigs = assigningConfigRepository.findByApprovalTypeOrderByLevelAsc(approvalType);

    final double variance = marginDiffPercent;
    return matchingConfigs.stream().filter(config -> {
        
        boolean amountInRange = chosenQuoteAmount >= config.getMinAmount() && 
                (config.getMaxAmount() == null || chosenQuoteAmount <= config.getMaxAmount());

        boolean varianceExceeded = variance > config.getMarginDifferencePercentage();
        
        return amountInRange || varianceExceeded;
    }).toList();
}

}
