package com.module.purchase.service;

import java.time.LocalDate;
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
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.RequestForQuotationStatus;
import com.module.purchase.repository.RequestForQuotationLineRepository;
import com.module.purchase.repository.RequestForQuotationRepository;
import com.module.purchase.specification.RequestForQuotationSpecification;

@Service
@Transactional
public class RequestForQuotationService {

    @Autowired
    private RequestForQuotationRepository rfqRepository;

    @Autowired
    private RequestForQuotationLineRepository rfqLineRepository;

    @Autowired
    private PurchaseRequestLineService purchaseRequestLineService;

    @Autowired
    private AuditLogsService auditLogsService;

    // SAVE BASE METHOD
    public RequestForQuotation saveRequestForQuotation(RequestForQuotation rfq) {
        return rfqRepository.save(rfq);
    }

    // CREATE NEW RFQ MASTER ENTITY
    public RequestForQuotation addRequestForQuotation(RequestForQuotation rfq, Employee employee) {
        if (rfq.getRequestedDate() == null) {
            rfq.setRequestedDate(LocalDate.now());
        }
        if (rfq.getStatus() == null) {
            rfq.setStatus(RequestForQuotationStatus.DRAFT);
        }

        rfq = saveRequestForQuotation(rfq);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REQUEST_FOR_QUOTATION);
        log.setEntityId(rfq.getId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        // auditLogsService.addAuditLog(log);

        return rfq;
    }

    // GET BY ID WITH EXPLICIT NOT-FOUND CHECK PROTECTION BLOCK
    public Optional<RequestForQuotation> getRequestForQuotationById(Long id) {
        Optional<RequestForQuotation> rfq = rfqRepository.findById(id);

        if (rfq.isEmpty()) {
            throw new ResourceNotFoundException("Request For Quotation not found with id: " + id);
        }

        return rfq;
    }

    // GET ALL UNFILTERED ROWS LIST
    public List<RequestForQuotation> getAllRequestsForQuotation() {
        return rfqRepository.findAll();
    }

    public Page<RequestForQuotation> getRequestsForQuotationPaged(RequestForQuotation filter, Pageable pageable) {
        if (filter == null) {
            filter = new RequestForQuotation();
        }

        Specification<RequestForQuotation> spec = Specification
                .where(RequestForQuotationSpecification.hasId(filter.getId()))
                .and(RequestForQuotationSpecification.hasStatus(filter.getStatus()))
                .and(RequestForQuotationSpecification.hasRequestedDate(filter.getRequestedDate()));

        return rfqRepository.findAll(spec, pageable);
    }

    // UPDATE RFQ HEADER ENTITY
    public RequestForQuotation updateRequestForQuotation(RequestForQuotation rfq, Employee employee) {
        // Enforce boundary verification checks first
        getRequestForQuotationById(rfq.getId());

        rfq = saveRequestForQuotation(rfq);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REQUEST_FOR_QUOTATION);
        log.setEntityId(rfq.getId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        // auditLogsService.addAuditLog(log);

        return rfq;
    }

    public void deleteRequestForQuotationById(Long id, Employee employee) {
        RequestForQuotation existingRfq = rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request For Quotation not found for deletion with id: " + id));

        if (existingRfq.getStatus() != RequestForQuotationStatus.DRAFT) {
            throw new IllegalStateException(
                    "Compliance Block: Cannot delete an RFQ that has already been published or closed.");
        }

        List<PurchaseRequestLine> linkedPrLines = purchaseRequestLineService.getRequestForQuotation(existingRfq);
        if (linkedPrLines != null && !linkedPrLines.isEmpty()) {
            for (PurchaseRequestLine prLine : linkedPrLines) {
                prLine.setRequestForQuotation(null); 
                purchaseRequestLineService.updatePurchaseRequestLine(prLine);
            }
        }


        if (existingRfq.getRequestForQuotationLines() != null && !existingRfq.getRequestForQuotationLines().isEmpty()) {
            rfqLineRepository.deleteAll(existingRfq.getRequestForQuotationLines());
        }

        rfqRepository.deleteById(id);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.REQUEST_FOR_QUOTATION);
        log.setEntityId(id);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        // auditLogsService.addAuditLog(log);
    }

    public RequestForQuotationLine addRfqLine(RequestForQuotationLine line) {
        return rfqLineRepository.save(line);
    }

    public List<RequestForQuotationLine> getLinesByRfqId(Long rfqId) {
        RequestForQuotation rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cannot load lines, RFQ master not found with id: " + rfqId));
        return rfqLineRepository.findByRequestForQuotation(rfq);
    }

    public void removeAllLinesByRfq(RequestForQuotation rfq) {
        List<RequestForQuotationLine> activeLines = rfqLineRepository.findByRequestForQuotation(rfq);
        if (activeLines != null && !activeLines.isEmpty()) {
            rfqLineRepository.deleteAll(activeLines);
        }
    }
}