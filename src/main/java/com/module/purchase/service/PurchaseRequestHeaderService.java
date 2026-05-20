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

import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.enums.Status;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;
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
    private AuditLogsService auditLogsService;

    public PurchaseRequestHeader savePurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        return purchaseRequestHeaderRepository.save(purchaseRequestHeader);
    }

    public PurchaseRequestHeader addPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader,Employee employee) {

        purchaseRequestHeader = savePurchaseRequestHeader(purchaseRequestHeader);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_REQUEST);
        log.setEntityId(purchaseRequestHeader.getPurchaseRequestId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseRequestHeader;
    }

     public Long countAll()
    {
        return purchaseRequestHeaderRepository.count();
    }

    public Long countByStatus(Status status)
    {
        return purchaseRequestHeaderRepository.countByStatus(status);
    }

    public Optional<PurchaseRequestHeader> getPurchaseRequestHeaderById(Long id) {
        Optional<PurchaseRequestHeader> existingPurchaseRequestHeader = purchaseRequestHeaderRepository.findById(id);
        if (!existingPurchaseRequestHeader.isPresent()) {
            throw new RuntimeException("Purchase request header not found with id: " + id);
        }
       // System.out.println(existingPurchaseRequestHeader);
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

public List<PurchaseRequestDTO> getRecentPurchaseRequests(
        PageRequest pageRequest) {

    return purchaseRequestMapper.toPurchaseRequestDTO(purchaseRequestHeaderRepository
            .findAllByOrderByPurchaseRequestIdDesc(pageRequest));
}

    public void deletePurchaseRequestHeaderById(Long Id) {
        purchaseRequestHeaderRepository.deleteById(Id);
    }

    public PurchaseRequestHeader updatePurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader,Employee employee) {
         purchaseRequestHeader = savePurchaseRequestHeader(purchaseRequestHeader);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_REQUEST);
        log.setEntityId(purchaseRequestHeader.getPurchaseRequestId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseRequestHeader;
    }

}
