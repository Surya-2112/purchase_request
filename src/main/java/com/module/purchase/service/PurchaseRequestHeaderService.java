package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.ApprovalType;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Status;
import com.module.purchase.mapper.PurchaseRequestMapper;
import com.module.purchase.repository.PurchaseRequestHeaderRepository;
import com.module.purchase.specification.PurchaseRequestSpecification;

@Service
@Transactional
public class PurchaseRequestHeaderService {

    @Autowired
    private PurchaseRequestHeaderRepository purchaseRequestHeaderRepository;

    @Autowired
    private PurchaseRequestMapper purchaseRequestMapper;

    @Autowired
    private UsersService userservice;

    @Autowired
    private PurchaseRequestLineService purchaseRequestLineSerivce;

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    @Lazy
    private AssigningApprovalsService assigningApprovalsService;

    public PurchaseRequestHeader savePurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        return purchaseRequestHeaderRepository.save(purchaseRequestHeader);
    }

    public PurchaseRequestHeader addPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader, Employee employee) {

        purchaseRequestHeader = savePurchaseRequestHeader(purchaseRequestHeader);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_REQUEST);
        log.setEntityId(purchaseRequestHeader.getPurchaseRequestId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseRequestHeader;
    }

    public Long countAll() {
        return purchaseRequestHeaderRepository.count();
    } 

    public Long countByStatus(Status status) {
        return purchaseRequestHeaderRepository.countByStatus(status);
    }

    public Optional<PurchaseRequestHeader> getPurchaseRequestHeaderById(Long id) {
        Optional<PurchaseRequestHeader> existingPurchaseRequestHeader = purchaseRequestHeaderRepository.findById(id);
        if (!existingPurchaseRequestHeader.isPresent()) {
            throw new ResourceNotFoundException("Purchase request header not found with id: " + id);
        }
        return existingPurchaseRequestHeader;
    }

    public Page<PurchaseRequestDTO> getAllPurchaseRequest(PurchaseRequestDTO purchaseRequestDTO, int page, int size) {

        Specification<PurchaseRequestHeader> spec = Specification
                .where(PurchaseRequestSpecification.hasPurchaseRequestId(purchaseRequestDTO.getPurchaseRequestId()))
                .and(PurchaseRequestSpecification.hasCreatedBy(purchaseRequestDTO.getCreatedBy()))
                .and(PurchaseRequestSpecification.hasForDepartment(purchaseRequestDTO.getForDepartment()))
                .and(PurchaseRequestSpecification.hasStatus(purchaseRequestDTO.getStatus()));

        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseRequestHeader> prpage = purchaseRequestHeaderRepository.findAll(spec, pageable);
        return prpage.map(purchaseRequestMapper::toPurchaseRequestDTO);
    }

    public Page<PurchaseRequestDTO> getCreatedByUser(PurchaseRequestDTO purchaseRequestDTO, Long userId, int page,
            int size) {
        Employee existEmployee = userservice.getUserById(userId).get().getEmployee();
        Specification<PurchaseRequestHeader> spec = Specification
                .where(PurchaseRequestSpecification.hasPurchaseRequestId(purchaseRequestDTO.getPurchaseRequestId()))
                .and(PurchaseRequestSpecification.hasCreatedBy(existEmployee))
                .and(PurchaseRequestSpecification.hasForDepartment(purchaseRequestDTO.getForDepartment()))
                .and(PurchaseRequestSpecification.hasStatus(purchaseRequestDTO.getStatus()));

        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseRequestHeader> prpage = purchaseRequestHeaderRepository.findAll(spec, pageable);
        return prpage.map(purchaseRequestMapper::toPurchaseRequestDTO);
    }

    public List<PurchaseRequestDTO> getRecentPurchaseRequests(PageRequest pageRequest) {

        return purchaseRequestMapper.toPurchaseRequestDTO(purchaseRequestHeaderRepository
                .findAllByOrderByPurchaseRequestIdDesc(pageRequest));
    }

    public void deletePurchaseRequestHeaderById(Long id, Employee employee) {

        purchaseRequestLineSerivce.deleteAllLine(getPurchaseRequestHeaderById(id).get());
        purchaseRequestHeaderRepository.deleteById(id);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_REQUEST);
        log.setEntityId(id);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);  
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }

    public PurchaseRequestHeader updatePurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader, Employee employee) {

        AuditLogs log = new AuditLogs();

        PurchaseRequestLine purchaseRequestLine=new PurchaseRequestLine();
        purchaseRequestLine.setPurchaseRequestHeader(purchaseRequestHeader);
        purchaseRequestLine.setStatus(null);
        List<PurchaseRequestLine> purchaseRequestLines = purchaseRequestLineSerivce.getPurchaseRequestLineList(purchaseRequestLine);

        switch (purchaseRequestHeader.getStatus()) {
            case CANCELLED -> {
                log.setAction(Action.CANCEL);
                List<AssigningApprovals> lines = assigningApprovalsService.getAssigningApprovalByTypeAndReferId(ApprovalType.PURCHASE_REQUEST, purchaseRequestHeader.getPurchaseRequestId());

                for (AssigningApprovals line : lines) {
                    if (line.getStatus() == Status.WAITING_APPROVAL) {
                        line.setStatus(Status.CANCELLED);
                        assigningApprovalsService.updateApprovals(line, employee);
                    }
                }

                for (PurchaseRequestLine line : purchaseRequestLines) {
                    line.setStatus(Status.CANCELLED);
                    purchaseRequestLineSerivce.updatePurchaseRequestLine(line, employee);
                }
            }
            case APPROVED -> {
                log.setAction(Action.APPROVE);
                for (PurchaseRequestLine line : purchaseRequestLines) {
                    if (line.getApprovedQuantity().equals(line.getRequestedQuantity())) {
                        line.setStatus(Status.APPROVED);
                    } else if (line.getApprovedQuantity() > 0) {
                        line.setStatus(Status.PARTIALLY_APPROVED);
                    } else {
                        line.setStatus(Status.REJECTED);
                    }
                    purchaseRequestLineSerivce.updatePurchaseRequestLine(line, employee);
                }
            }
            case REJECTED -> {
                log.setAction(Action.REJECT);
                for (PurchaseRequestLine line : purchaseRequestLines) {
                    line.setStatus(Status.REJECTED);
                    purchaseRequestLineSerivce.updatePurchaseRequestLine(line, employee);
                }
            }
            default ->
                log.setAction(Action.UPDATE);
        }
        purchaseRequestHeader = savePurchaseRequestHeader(purchaseRequestHeader);

        log.setEntityType(EntityType.PURCHASE_REQUEST);
        log.setEntityId(purchaseRequestHeader.getPurchaseRequestId());
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseRequestHeader;
    }

    public Long countAllByEmployee(Employee employee) {
        if (employee == null) {
            return 0L;
        }
        return purchaseRequestHeaderRepository.countByCreatedBy(employee);
    }

    public Long countByStatusAndEmployee(Status status, Employee employee) {
        if (employee == null) {
            return 0L;
        }
        return purchaseRequestHeaderRepository.countByStatusAndCreatedBy(status, employee);
    }

    public List<PurchaseRequestDTO> getRecentPurchaseRequestsByEmployee(Employee employee, org.springframework.data.domain.Pageable pageable) {
        if (employee == null) {
            return List.of();
        }
        List<PurchaseRequestHeader> headers = purchaseRequestHeaderRepository
                .findByCreatedByOrderByPurchaseRequestIdDesc(employee, pageable);

        return purchaseRequestMapper.toPurchaseRequestDTO(headers);
    }

    public List<PurchaseRequestHeader> getPurchaseRequestList(PurchaseRequestDTO purchaseRequestDTO) {

        Specification<PurchaseRequestHeader> spec = Specification
                .where(PurchaseRequestSpecification.hasPurchaseRequestId(purchaseRequestDTO.getPurchaseRequestId()))
                .and(PurchaseRequestSpecification.hasCreatedBy(purchaseRequestDTO.getCreatedBy()))
                .and(PurchaseRequestSpecification.hasForDepartment(purchaseRequestDTO.getForDepartment()))
                .and(PurchaseRequestSpecification.hasStatus(purchaseRequestDTO.getStatus()));

        return purchaseRequestHeaderRepository.findAll(spec);
    }

    public Long getCountPurchaseRequest(PurchaseRequestDTO purchaseRequestDTO) {

        Specification<PurchaseRequestHeader> spec = Specification
                .where(PurchaseRequestSpecification.hasPurchaseRequestId(purchaseRequestDTO.getPurchaseRequestId()))
                .and(PurchaseRequestSpecification.hasCreatedBy(purchaseRequestDTO.getCreatedBy()))
                .and(PurchaseRequestSpecification.hasForDepartment(purchaseRequestDTO.getForDepartment()))
                .and(PurchaseRequestSpecification.hasStatus(purchaseRequestDTO.getStatus()));

        return purchaseRequestHeaderRepository.count(spec);
    }
}
