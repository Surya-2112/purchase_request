package com.module.purchase.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.module.purchase.repository.PurchaseOrderLineRepository;
import com.module.purchase.specification.PurchaseOrderLineSpecification;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.PurchaseOrderLine;

import java.util.Optional;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseOrderLineService {
    
    @Autowired
    private PurchaseOrderLineRepository purchaseOrderLineRepository;

    public PurchaseOrderLine savePurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        return purchaseOrderLineRepository.save(purchaseOrderLine);
    }

    public Optional<PurchaseOrderLine> getPurchaseOrderLineById(Long id) {
        Optional<PurchaseOrderLine> existingPurchaseOrderLine = purchaseOrderLineRepository.findById(id);
        if (!existingPurchaseOrderLine.isPresent()) {
            throw new ResourceNotFoundException("Purchase order line not found with id: " + id);
        }
        return existingPurchaseOrderLine;
    }
   

    public PurchaseOrderLine addPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        return savePurchaseOrderLine(purchaseOrderLine);
    }

    public List<PurchaseOrderLine> getAllPurchaseOrderLines() {
        return purchaseOrderLineRepository.findAll();
    }

    public List<PurchaseOrderLine> getPurchaseOrderList(PurchaseOrderLine purchaseOrderline) {

        Specification<PurchaseOrderLine> spec = Specification
                .where(PurchaseOrderLineSpecification.hasId(purchaseOrderline.getId()))
                .and(PurchaseOrderLineSpecification.hasPurchaseOrderHeader(purchaseOrderline.getPurchaseOrderHeader()))
                .and(PurchaseOrderLineSpecification.hasItemVariant(purchaseOrderline.getItemVariant()));

        return purchaseOrderLineRepository.findAll(spec);
    }

    public Long getCountPurchaseOrderList(PurchaseOrderLine purchaseOrderline) {

        Specification<PurchaseOrderLine> spec = Specification
                .where(PurchaseOrderLineSpecification.hasId(purchaseOrderline.getId()))
                .and(PurchaseOrderLineSpecification.hasPurchaseOrderHeader(purchaseOrderline.getPurchaseOrderHeader()))
                .and(PurchaseOrderLineSpecification.hasItemVariant(purchaseOrderline.getItemVariant()));

        return purchaseOrderLineRepository.count(spec);
    }

}
