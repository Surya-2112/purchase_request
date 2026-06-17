package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AssigningApprovals;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.RepeatedPeriodReferType;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.enums.Status;
import com.module.purchase.repository.RepeatedPeriodRepository;
import com.module.purchase.specification.RepeatedPeriodSpecification;

@Service
@Transactional
public class RepeatedPeriodService {

    @Autowired
    private RepeatedPeriodRepository repeatedPeriodRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    @Lazy
    @Autowired
    private PurchaseRequestLineService purchaseRequestLineService;

    @Autowired
    private PurchaseRequestHeaderService purchaseRequestHeaderService;

    @Autowired
    private AssigningApprovalsService assigningApprovalsService;

    @Autowired
    private RequestForQuotationService requestForQuotationService; 

    @Autowired
    private CategoryService categoryService;

    public RepeatedPeriod save(RepeatedPeriod repeatedPeriod) {
        return repeatedPeriodRepository.save(repeatedPeriod);
    }

    public Optional<RepeatedPeriod> findByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId) {
        return repeatedPeriodRepository.findByReferTypeAndReferId(referType, referId);
    }

    public RepeatedPeriod addRepeatedPeriod(RepeatedPeriod repeatedPeriod, Employee employee) {
        if (repeatedPeriod.getNextDate() == null) {
            repeatedPeriod.setNextDate(repeatedPeriod.getFromDate());
        }

        repeatedPeriod = save(repeatedPeriod);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REPEATED_PERIOD); 
        log.setEntityId(repeatedPeriod.getId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        auditLogsService.addAuditLog(log);

        return repeatedPeriod;
    }

    public Optional<RepeatedPeriod> getRepeatedPeriodById(Long id) {
        Optional<RepeatedPeriod> period = repeatedPeriodRepository.findById(id);

        if (period.isEmpty()) {
            throw new ResourceNotFoundException("Repeated period schedule not found with id: " + id);
        }

        return period;
    }

    public Page<RepeatedPeriod> getAllRepeatedPeriodsPaged(RepeatedPeriod filter, Pageable pageable) {
        
        if (filter == null) {
            filter = new RepeatedPeriod();
        }

        Specification<RepeatedPeriod> spec = Specification
                .where(RepeatedPeriodSpecification.hasId(filter.getId()))
                .and(RepeatedPeriodSpecification.hasReferType(filter.getReferType()))
                .and(RepeatedPeriodSpecification.hasReferId(filter.getReferId()))
                .and(RepeatedPeriodSpecification.hasFrequencyType(filter.getFrequencyType()))
                .and(RepeatedPeriodSpecification.hasNextDate(filter.getNextDate()));

        return repeatedPeriodRepository.findAll(spec, pageable);
    }

    public RepeatedPeriod updateRepeatedPeriod(RepeatedPeriod repeatedPeriod, Employee employee) {
        getRepeatedPeriodById(repeatedPeriod.getId());

        repeatedPeriod = save(repeatedPeriod);
        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REPEATED_PERIOD);
        log.setEntityId(repeatedPeriod.getId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
        return repeatedPeriod;
    }

    public void deleteRepeatedPeriodById(Long id, Employee employee) {
        getRepeatedPeriodById(id); 

        repeatedPeriodRepository.deleteById(id);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REPEATED_PERIOD);
        log.setEntityId(id);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        auditLogsService.addAuditLog(log);
    }

    public void deleteByReferTypeAndReferId(RepeatedPeriodReferType referType, Long referId) {
        repeatedPeriodRepository.deleteByReferTypeAndReferId(referType, referId);
    }

    public void assignedRepeatedTask()
    {  
        List<RepeatedPeriod> pendingTasks= repeatedPeriodRepository.findPendingTasks(LocalDate.now(),RequestForQuotationStatus.OPEN);
        for(RepeatedPeriod task:pendingTasks)
        { 
            if(task.getReferType().equals(RepeatedPeriodReferType.PURCHASE_REQUEST_LINE))
            {
                PurchaseRequestLine newPrLine=new PurchaseRequestLine();
                PurchaseRequestHeader newHeader= new PurchaseRequestHeader();
                PurchaseRequestLine referline= purchaseRequestLineService.getPurchaseRequestLineById(task.getReferId()).orElse(null);
                if(referline==null)
                {   task.setNextDate(null);
                    task.setStatus(RequestForQuotationStatus.CLOSED);
                    updateRepeatedPeriod(task,null);
                    continue;
                }
                newPrLine.setItemVariant(referline.getItemVariant());
                newPrLine.setRequestedQuantity(referline.getRequestedQuantity());
                newPrLine.setDescription("");
                newPrLine.setItemUnitPrice(referline.getItemUnitPrice());
                newPrLine.setStatus(Status.WAITING_APPROVAL);
                newPrLine.setItemTotalAmount(referline.getItemTotalAmount());

                newHeader.setCreatedBy(referline.getPurchaseRequestHeader().getCreatedBy());
                newHeader.setCreatedDate(LocalDate.now());
                newHeader.setStatus(Status.WAITING_APPROVAL);
                newHeader.setForDepartment(referline.getPurchaseRequestHeader().getForDepartment());
                newHeader.setLevel(referline.getPurchaseRequestHeader().getLevel());
                newHeader.setTotalAmount(newPrLine.getItemTotalAmount());

                newHeader=purchaseRequestHeaderService.addPurchaseRequestHeader(newHeader, null);
                newPrLine.setPurchaseRequestHeader(newHeader);
                purchaseRequestLineService.addPurchaseRequestLine(newPrLine);

                for(AssigningApprovals approvals:assigningApprovalsService.getAssigningApprovalByTypeAndReferId(ApprovalType.PURCHASE_REQUEST, referline.getPurchaseRequestHeader().getPurchaseRequestId()))
                {  AssigningApprovals newApproval=new AssigningApprovals();

                    newApproval.setApprovalType(approvals.getApprovalType());
                    newApproval.setAssignedBy(approvals.getAssignedBy());
                    newApproval.setEmployeeGroup(approvals.getEmployeeGroup());
                    newApproval.setAssignedDate(LocalDate.now());
                    newApproval.setLevel(approvals.getLevel());
                    newApproval.setStatus(Status.DRAFT);
                    newApproval.setSource(approvals.getSource());
                    newApproval.setReferenceId(newHeader.getPurchaseRequestId());
                    assigningApprovalsService.addApprovals(newApproval,null);
                }
            }
            else if(task.getReferType().equals(RepeatedPeriodReferType.CATEGORY)){

                List<PurchaseRequestLine> prLines = purchaseRequestLineService.getPurchaseLinesByCategory(categoryService.getCategoryById(task.getReferId()).get());
               if(!prLines.isEmpty())
                 {   
                RequestForQuotation rfq=new RequestForQuotation();
                    rfq.setRequestedDate(task.getNextDate());
                    rfq.setRequestEndDate(LocalDate.now().plusDays(7));
                    rfq.setStatus(RequestForQuotationStatus.DRAFT);
                    requestForQuotationService.addRequestForQuotation(rfq, null);

                RequestForQuotationLine rfqLine=new RequestForQuotationLine();
                rfqLine.setItemVariant(prLines.get(0).getItemVariant());
                rfqLine.setRequestedQuantity(0.0);
                rfqLine.setRequestForQuotation(rfq);
                for(PurchaseRequestLine line: prLines)
                {
                    if(!rfqLine.getItemVariant().equals(line.getItemVariant())){
                        requestForQuotationService.addRfqLine(rfqLine);
                        rfqLine=new RequestForQuotationLine();
                        rfqLine.setItemVariant(line.getItemVariant());
                        rfqLine.setRequestedQuantity(0.0);
                        rfqLine.setRequestForQuotation(rfq);
                    }
                    rfqLine.setRequestedQuantity(line.getApprovedQuantity()+rfqLine.getRequestedQuantity());
                    line.setRequestForQuotation(rfq);
                    purchaseRequestLineService.updatePurchaseRequestLine(line, null);
                }
                requestForQuotationService.addRfqLine(rfqLine);
            }
                
            }
            task.setNextDate(task.getFrequencyType().calculateNext(task.getNextDate(),task.getFrequencyPeriod()));
            if(task.getToDate()!=null)
            {task.setNextDate(task.getNextDate().isAfter(task.getToDate())?null:task.getNextDate());}
        }
    }
}