package com.module.purchase.service;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.Year;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.context.annotation.Lazy;

import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;
import com.module.purchase.repository.AssigningApprovalsRepository;
import com.module.purchase.specification.AssigningApprovalsSpecification;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entityDTO.AssigningApprovalsDTO;
import com.module.purchase.entity.Employee;
import com.module.purchase.mapper.AssigningApprovalsMapper;

import java.time.LocalDate;
import java.util.List;


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
    @Lazy
    private PurchaseRequestHeaderService purchaseRequestHeaderService;

    @Autowired
    private PurchaseOrderHeaderService purchaseOrderHeaderService;

    @Autowired
    private DepartmentBudgetService departmentBudgetService;

    @Autowired
    private AuditLogsService auditLogsService;

    public AssigningApprovals saveAssigningApproval(AssigningApprovals assigningApproval) {
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
    public AssigningApprovals getAssigningApprovalByTypeAndReferIdAndLevle(ApprovalType approvalType,Long referenceId,Integer level)
    {
     Optional<AssigningApprovals> exist=assigningApprovalsRepository.findByApprovalTypeAndReferenceIdAndLevel(approvalType,referenceId,level);
     return exist.get();
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

    public Page<AssigningApprovalsDTO> getPurchaseOrderApprovalsForMe(AssigningApprovalsDTO assigningApprovalsDTO,Long userId,int page, int size) {

        Specification<AssigningApprovals> spec = Specification
        .where(AssigningApprovalsSpecification.hasAssigningApprovalsId(assigningApprovalsDTO.getAssigningApprovalsId()))
        .and(AssigningApprovalsSpecification.hasApprover(userService.getUserById(userId).get().getEmployee()))
        .and(AssigningApprovalsSpecification.hasApprovalType(ApprovalType.PURCHASE_ORDER_APPROVAL))
        .and(AssigningApprovalsSpecification.hasStatus(assigningApprovalsDTO.getStatus()))
        .and(AssigningApprovalsSpecification.hasReferenceId(assigningApprovalsDTO.getReferenceId()));

        Pageable pageable = PageRequest.of(page, size);

        Page<AssigningApprovals> approvalsPage = assigningApprovalsRepository.findAll(spec, pageable);

        return approvalsPage.map(assigningApprovalsMapper::toAssigningApprovalsDTO);
    }

     public AssigningApprovals addApprovals(AssigningApprovals assigningApproval,Employee employee) {
        if(assigningApproval.getLevel()==1)
        {
            assigningApproval.setStatus(Status.WAITING_APPROVAL);
        }
        assigningApproval=assigningApprovalsRepository.save(assigningApproval);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.ASSIGNING_APPROVAL);
        log.setEntityId(assigningApproval.getAssigningApprovalsId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        return assigningApproval;
    }

    public AssigningApprovals updateApprovals(AssigningApprovals assigningApprovals,Employee employee)
    {   
        AuditLogs log = new AuditLogs();
        AssigningApprovals exist = getAssigningApprovalById(assigningApprovals.getAssigningApprovalsId()).get();
        if(assigningApprovals.getApprovalType()==ApprovalType.PURCHASE_REQUEST_APPROVAL){
        PurchaseRequestHeader purchaseRequestHeader=purchaseRequestHeaderService.getPurchaseRequestHeaderById(exist.getReferenceId()).get();
        if(assigningApprovals.getStatus()==Status.APPROVED)
        {   log.setAction(Action.APPROVE);
           if(purchaseRequestHeader.getLevel()> assigningApprovals.getLevel())
           {AssigningApprovals next = getAssigningApprovalByTypeAndReferIdAndLevle(
                                ApprovalType.PURCHASE_REQUEST_APPROVAL,
                                purchaseRequestHeader.getPurchaseRequestId(),
                                assigningApprovals.getLevel()+1);
            next.setStatus(Status.WAITING_APPROVAL);
            saveAssigningApproval(next);
           }else{
                purchaseOrderHeaderService.genratepurchaseOrder(purchaseRequestHeader);
                purchaseRequestHeader.setStatus(Status.APPROVED);
                purchaseRequestHeaderService.updatePurchaseRequestHeader(purchaseRequestHeader,null);
           }
        }else if(assigningApprovals.getStatus()==Status.REJECTED){
            log.setAction(Action.REJECT);
            purchaseRequestHeader.setStatus(Status.REJECTED);
            purchaseRequestHeaderService.updatePurchaseRequestHeader(purchaseRequestHeader,null);
        }} else{
     
        PurchaseOrderHeader purchaseOrderHeader= purchaseOrderHeaderService.getPurchaseOrderHeaderById(exist.getReferenceId()).get();
        if(assigningApprovals.getStatus()==Status.APPROVED)
        {   
            log.setAction(Action.APPROVE);
           if(purchaseOrderHeader.getLevel()> assigningApprovals.getLevel())
           {AssigningApprovals next = getAssigningApprovalByTypeAndReferIdAndLevle(
                                ApprovalType.PURCHASE_ORDER_APPROVAL,
                                purchaseOrderHeader.getPurchaseOrderId(),
                                assigningApprovals.getLevel()+1);
            next.setStatus(Status.WAITING_APPROVAL);
            saveAssigningApproval(next);
           }else{
                purchaseOrderHeader.setStatus(Status.APPROVED);
                purchaseOrderHeader.setExpectedDeliveryDate(LocalDate.now().plusDays(10));
                purchaseOrderHeaderService.updatePurchaseOrderHeader(purchaseOrderHeader,null);
           }
        }else if(assigningApprovals.getStatus()==Status.REJECTED){
            purchaseOrderHeader.setStatus(Status.REJECTED);
            log.setAction(Action.REJECT);

            DepartmentBudget  existBudget=departmentBudgetService.getByDepartmentAndYear(
                    purchaseOrderHeader.getPurchaseRequestHeader().getForDepartment(),Year.now());
                    existBudget.setRemainingBudgetAmount(purchaseOrderHeader.getTotalAmount()+existBudget.getRemainingBudgetAmount());
            departmentBudgetService.updateDepartmentBudget(existBudget,employee);

            purchaseOrderHeaderService.updatePurchaseOrderHeader(purchaseOrderHeader,null);
        }
        } 
        if(assigningApprovals.getStatus()==Status.CANCELLED)
        {
            log.setAction(Action.CANCEL);
        }

        log.setEntityType(EntityType.ASSIGNING_APPROVAL);
        log.setEntityId(assigningApprovals.getAssigningApprovalsId());
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        return saveAssigningApproval(assigningApprovals);
    }

}
