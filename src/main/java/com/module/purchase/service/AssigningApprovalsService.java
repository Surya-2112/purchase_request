package com.module.purchase.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.repository.AssigningApprovalsRepository;
import com.module.purchase.specification.AssigningApprovalsSpecification;

import jakarta.transaction.Transactional;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.mapper.AssigningApprovalsMapper;

import java.util.List;

import com.module.purchase.enums.Status;

@Service
@Transactional
public class AssigningApprovalsService {

    @Autowired
    private AssigningApprovalsRepository assigningApprovalsRepository;

    @Autowired
    private AssigningApprovalsMapper assigningApprovalsMapper;

    @Autowired 
    private UsersService userService;

    @Autowired
    private PurchaseRequestHeaderService purchaseRequestHeaderService;

    public AssigningApprovals saveAssigningApproval(AssigningApprovals assigningApproval) {
        return assigningApprovalsRepository.save(assigningApproval);
    }

    public AssigningApprovals addApprovals(AssigningApprovals assigningApproval) {
        if(assigningApproval.getLevel()==1)
        {
            assigningApproval.setStatus(Status.WAITING_APPROVAL);
        }
        return assigningApprovalsRepository.save(assigningApproval);
    }
    
    public Optional<AssigningApprovals> getAssigningApprovalById(Long id) {
        Optional<AssigningApprovals> existingApproval = assigningApprovalsRepository.findById(id);
        if (!existingApproval.isPresent()) {
            throw new RuntimeException("Assigning approval not found with id: " + id);
        }
        return existingApproval;
    }

    public List<AssigningApprovals>getAssigningApprovalByTypeAndReferId(ApprovalType approvalType,Long referenceId)
    {
        return assigningApprovalsRepository.findByApprovalTypeAndReferenceId(approvalType,referenceId);
    }

    public List<AssigningApprovals> getAllApprovals() {
        return assigningApprovalsRepository.findAll();
    }

    public Page<AssigningApprovalsDTO> getPurchaseRequestApprovalsForMe(AssigningApprovalsDTO assigningApprovalsDTO,Long userId,int page, int size) {

        Specification<AssigningApprovals> spec = Specification
        .where(AssigningApprovalsSpecification.hasAssigningApprovalsId(assigningApprovalsDTO.getAssigningApprovalsId()))
        .and(AssigningApprovalsSpecification.hasApprover(userService.getUserById(userId).get().getEmployee()))
        .and(AssigningApprovalsSpecification.hasApprovalType(ApprovalType.PURCHASE_REQUEST_APPROVAL))
        .and(AssigningApprovalsSpecification.hasStatus(assigningApprovalsDTO.getStatus()))
        .and(AssigningApprovalsSpecification.hasReferenceId(assigningApprovalsDTO.getReferenceId()));

        Pageable pageable = PageRequest.of(page, size);

        Page<AssigningApprovals> approvalsPage = assigningApprovalsRepository.findAll(spec, pageable);

        return approvalsPage.map(assigningApprovalsMapper::toAssigningApprovalsDTO);
    }

    public AssigningApprovals updateApprovals(AssigningApprovals assigningApprovals)
    {   
        AssigningApprovals exist=getAssigningApprovalById(assigningApprovals.getAssigningApprovalsId()).get();
        PurchaseRequestHeader purchaseRequestHeader=purchaseRequestHeaderService.getPurchaseRequestHeaderById(exist.getReferenceId()).get();
        if(assigningApprovals.getStatus()==Status.APPROVED)
        {
           if(purchaseRequestHeader.getLevel()> assigningApprovals.getLevel())
           {
                getAssigningApprovalByTypeAndReferId(ApprovalType.PURCHASE_REQUEST_APPROVAL,purchaseRequestHeader.getPurchaseRequestId());
                
           }else{

           }
        }
        return exist;
    }

}
