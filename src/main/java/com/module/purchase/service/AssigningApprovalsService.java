package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EmployeeGroup; // Ensure this is imported
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Status;
import com.module.purchase.mapper.AssigningApprovalsMapper;
import com.module.purchase.repository.AssigningApprovalsRepository;
import com.module.purchase.specification.AssigningApprovalsSpecification;

@Service
@Transactional
public class AssigningApprovalsService {

    @Autowired
    private AssigningApprovalsRepository assigningApprovalsRepository;

    @Autowired
    private AssigningApprovalsMapper assigningApprovalsMapper;

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    private NeedsService needsService;
    
    @Autowired
    private PurchaseRequestHeaderService purchaseRequestHeaderService;

    public AssigningApprovals saveAssigningApproval(AssigningApprovals assigningApproval) {
        return assigningApprovalsRepository.save(assigningApproval);
    }

    public Optional<AssigningApprovals> getAssigningApprovalById(Long id) {
        Optional<AssigningApprovals> existingApproval = assigningApprovalsRepository.findById(id);
        if (!existingApproval.isPresent()) {
            throw new ResourceNotFoundException("Assigning approval not found with id: " + id);
        }
        return existingApproval;
    }

    public List<AssigningApprovals> getAssigningApprovalByTypeAndReferId(ApprovalType approvalType, Long referenceId) {
        return assigningApprovalsRepository.findByApprovalTypeAndReferenceId(approvalType, referenceId);
    }

    public List<AssigningApprovals> getAllApprovals() {
        return assigningApprovalsRepository.findAll();
    }
    
    public AssigningApprovals getAssigningApprovalByTypeAndReferIdAndLevle(ApprovalType approvalType, Long referenceId, Integer level) {
         Optional<AssigningApprovals> exist = assigningApprovalsRepository.findByApprovalTypeAndReferenceIdAndLevel(approvalType, referenceId, level);
         return exist.get();
    }   

    public Page<AssigningApprovalsDTO> getPurchaseRequestApprovalsForMyGroup(
            AssigningApprovalsDTO assigningApprovalsDTO, 
            EmployeeGroup group, 
            int page, 
            int size) {

        Specification<AssigningApprovals> spec = Specification
            .where(AssigningApprovalsSpecification.hasAssigningApprovalsId(assigningApprovalsDTO.getAssigningApprovalsId()))
            .and(AssigningApprovalsSpecification.hasEmployeeGroup(group)) 
            .and(AssigningApprovalsSpecification.hasApprovalType(ApprovalType.PURCHASE_REQUEST))
            .and(AssigningApprovalsSpecification.hasStatus(assigningApprovalsDTO.getStatus()))
            .and(AssigningApprovalsSpecification.hasReferenceId(assigningApprovalsDTO.getReferenceId()));

        Pageable pageable = PageRequest.of(page, size);

        Page<AssigningApprovals> approvalsPage = assigningApprovalsRepository.findAll(spec, pageable);

        return approvalsPage.map(assigningApprovalsMapper::toAssigningApprovalsDTO);
    }

    public AssigningApprovals addApprovals(AssigningApprovals assigningApproval, Employee employee) {
        if (needsService.getSpecificNeedRecord(EntityType.ITEM, assigningApproval.getReferenceId()).isEmpty()&& assigningApproval.getLevel() == 1) {
            assigningApproval.setStatus(Status.WAITING_APPROVAL);
        }
        assigningApproval = assigningApprovalsRepository.save(assigningApproval);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.ASSIGNING_APPROVAL);
        log.setEntityId(assigningApproval.getAssigningApprovalsId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        return assigningApproval;
    }

    public AssigningApprovals updateApprovals(AssigningApprovals assigningApprovals, Employee employee) {   
      
        AuditLogs log = new AuditLogs();
        AssigningApprovals exist = getAssigningApprovalById(assigningApprovals.getAssigningApprovalsId()).get();
      
        PurchaseRequestHeader purchaseRequestHeader=purchaseRequestHeaderService.getPurchaseRequestHeaderById(exist.getReferenceId()).get();
        if(assigningApprovals.getStatus()==Status.APPROVED)
        {   log.setAction(Action.APPROVE);
           if(purchaseRequestHeader.getLevel()> assigningApprovals.getLevel())
           {AssigningApprovals next = getAssigningApprovalByTypeAndReferIdAndLevle(
                                ApprovalType.PURCHASE_REQUEST,
                                purchaseRequestHeader.getPurchaseRequestId(),
                                assigningApprovals.getLevel()+1);
            next.setStatus(Status.WAITING_APPROVAL);
            saveAssigningApproval(next);
           }else{
                purchaseRequestHeader.setStatus(Status.APPROVED);
                purchaseRequestHeaderService.updatePurchaseRequestHeader(purchaseRequestHeader,null);
           }
        }else if(assigningApprovals.getStatus()==Status.REJECTED){
            log.setAction(Action.REJECT);
            purchaseRequestHeader.setStatus(Status.REJECTED);
            purchaseRequestHeaderService.updatePurchaseRequestHeader(purchaseRequestHeader,null);
        }
        else if(assigningApprovals.getStatus()==Status.CANCELLED)
        {
            log.setAction(Action.CANCEL);
        } else{
            log.setAction(Action.UPDATE);
        }
        log.setEntityType(EntityType.ASSIGNING_APPROVAL);
        log.setEntityId(assigningApprovals.getAssigningApprovalsId());
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        return saveAssigningApproval(assigningApprovals);
    }
}