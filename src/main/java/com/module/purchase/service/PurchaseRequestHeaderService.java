package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.repository.PurchaseRequestHeaderRepository;
import com.module.purchase.specification.PurchaseRequestSpecification;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseRequestHeader;

import java.util.Optional;
import java.util.List;

import com.module.purchase.entityDTO.PurchaseRequestDTO;
import com.module.purchase.mapper.PurchaseRequestMapper;
  

@Service
@Transactional
public class PurchaseRequestHeaderService {
    
    @Autowired
    private PurchaseRequestHeaderRepository purchaseRequestHeaderRepository;

    @Autowired
    private PurchaseRequestMapper purchaseRequestMapper;

    @Autowired 
    private UsersService userservice;

    public PurchaseRequestHeader savePurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        return purchaseRequestHeaderRepository.save(purchaseRequestHeader);
    }

    public PurchaseRequestHeader addPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        return savePurchaseRequestHeader(purchaseRequestHeader);
    }

    public Optional<PurchaseRequestHeader> getPurchaseRequestHeaderById(Long id) {
        Optional<PurchaseRequestHeader> existingPurchaseRequestHeader = purchaseRequestHeaderRepository.findDetailsById(id);
        if (!existingPurchaseRequestHeader.isPresent()) {
            throw new RuntimeException("Purchase request header not found with id: " + id);
        }
        return existingPurchaseRequestHeader;
    }

    public List<PurchaseRequestHeader> getPurchaseRequestHeaders() {
        return purchaseRequestHeaderRepository.findAll();
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

     public Page<PurchaseRequestDTO> getCreatedByUser(PurchaseRequestDTO purchaseRequestDTO,Long userId,int page, int size) {
        Employee existEmployee= userservice.getUserById(userId).get().getEmployee();
        Specification<PurchaseRequestHeader> spec = Specification
                .where(PurchaseRequestSpecification.hasPurchaseRequestId(purchaseRequestDTO.getPurchaseRequestId()))
                .and(PurchaseRequestSpecification.hasCreatedBy(existEmployee))
                .and(PurchaseRequestSpecification.hasForDepartment(purchaseRequestDTO.getForDepartment()))
                .and(PurchaseRequestSpecification.hasStatus(purchaseRequestDTO.getStatus()));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseRequestHeader> prpage = purchaseRequestHeaderRepository.findAll(spec, pageable);
        return prpage.map(purchaseRequestMapper::toPurchaseRequestDTO);
    }

    public void deletePurchaseRequestHeaderById(Long Id)
    {
       purchaseRequestHeaderRepository.deleteById(Id);
    }
}
