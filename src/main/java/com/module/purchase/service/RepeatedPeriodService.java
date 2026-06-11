package com.module.purchase.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RepeatedPeriod;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.RepeatedPeriodReferType;
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

    @Autowired
    private PurchaseRequestLineService purchaseRequestLineService;

    @Autowired
    private PurchaseRequestHeaderService purchaseRequestHeaderService;

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
    {   System.out.println("-----------------------8 RepeatedPeriod is called ----------------------");
        List<RepeatedPeriod> pendingTasks= repeatedPeriodRepository.findPendingTasks(LocalDate.now());
    //    List<RequestForQuotationLine> rfqlines = new ArrayList<RequestForQuotationLine>();
        for(RepeatedPeriod task:pendingTasks)
        {
            if(task.getReferType().equals(RepeatedPeriodReferType.PURCHASE_REQUEST_LINE))
            {
                PurchaseRequestLine newPrLine=new PurchaseRequestLine();
                PurchaseRequestHeader newHeader= new PurchaseRequestHeader();
                PurchaseRequestLine referline= purchaseRequestLineService.getPurchaseRequestLineById(task.getId()).orElse(null);
                if(referline==null)
                {   task.setNextDate(null);
                    updateRepeatedPeriod(task,null);
                    continue;
                }
                newPrLine.setItemVariant(referline.getItemVariant());
                newPrLine.setRequestedQuantity(referline.getRequestedQuantity());
                newPrLine.setDescription("");
                newPrLine.setItemUnitPrice(referline.getItemUnitPrice());
                newPrLine.setStatus(Status.DRAFT);
                newPrLine.setItemTotalAmount(referline.getItemTotalAmount());

                newHeader.setCreatedBy(null);
                newHeader.setCreatedDate(LocalDate.now());
                newHeader.setStatus(Status.DRAFT);
                newHeader.setForDepartment(referline.getPurchaseRequestHeader().getForDepartment());
                newHeader.setLevel(1);
                newHeader.setTotalAmount(newPrLine.getItemTotalAmount());

                purchaseRequestHeaderService.addPurchaseRequestHeader(newHeader, null);
                newPrLine.setPurchaseRequestHeader(newHeader);
                purchaseRequestLineService.addPurchaseRequestLine(newPrLine);
            }
            else{

            }
            task.setNextDate(task.getFrequencyType().calculateNext(LocalDate.now(),task.getFrequencyPeriod()));
        }
    }
}