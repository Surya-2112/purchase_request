package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotationLine;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.repository.ItemVariantRepository;
import com.module.purchase.specification.ItemVariantSpecification;

@Service
@Transactional
public class ItemVariantService {

    @Autowired
    private ItemVariantRepository itemVariantRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    private PurchaseOrderLineService purchaseOrderLineService;

    @Autowired
    @Lazy
    private PurchaseRequestLineService purchaseRequestLineService;

    @Autowired
    private QuotationService quotationLineService;

    @Autowired
    private RequestForQuotationService requestForQuotationService;

    public ItemVariant saveItemVariant(ItemVariant itemVariant) {
        return itemVariantRepository.save(itemVariant);
    }

    public ItemVariant addItemVariant(  ItemVariant itemVariant,  Employee employee) {

        itemVariant = saveItemVariant(itemVariant);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.ITEM_VARIANT);
        log.setEntityId(itemVariant.getId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        auditLogsService.addAuditLog(log);

        return itemVariant;
    }

    public Optional<ItemVariant> getItemVariantById(Long id) {

        Optional<ItemVariant> itemVariant =
                itemVariantRepository.findById(id);

        if (itemVariant.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Item Variant not found with id: " + id);
        }

        return itemVariant;
    }

    public List<ItemVariant> getItemVariants() {
        return itemVariantRepository.findAll();
    }

     public List<ItemVariant> getItemVariantsByItem(Item item) {

        return itemVariantRepository.findByItemAndActive(item,true);
    }

    public Page<ItemVariant> getAllItemVariants(ItemVariant itemVariant, int page, int size) {

        Specification<ItemVariant> spec = Specification
                .where(ItemVariantSpecification.hasId(itemVariant.getId()))
                .and(ItemVariantSpecification.hasItem(itemVariant.getItem()))
                .and(ItemVariantSpecification.hasSpecification(itemVariant.getSpecification()))
                .and(ItemVariantSpecification.hasActive(itemVariant.getActive()));

        PageRequest pageable = PageRequest.of(page, size);

        return itemVariantRepository.findAll(spec, pageable);
    }

    public ItemVariant updateItemVariant(ItemVariant itemVariant,Employee employee) {

        getItemVariantById(itemVariant.getId()).get();

        itemVariant = saveItemVariant(itemVariant);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.ITEM_VARIANT);
        log.setEntityId(itemVariant.getId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        auditLogsService.addAuditLog(log);

        return itemVariant;
    }

    public void deleteItemVariantById(Long id, Employee employee) {

        ItemVariant existing = getItemVariantById(id).get();

        PurchaseOrderLine purchaseOrderLine=new PurchaseOrderLine();
        purchaseOrderLine.setItemVariant(existing);
        Long poLineCount=purchaseOrderLineService.getCountPurchaseOrderList(purchaseOrderLine);

        PurchaseRequestLine purchaseRequestLine=new PurchaseRequestLine();
        purchaseRequestLine.setItemVariant(existing);
        Long prLineCount=purchaseRequestLineService.getCountPurchaseRequestLineList(purchaseRequestLine);

        QuotationLine quotationLine=new QuotationLine();
        quotationLine.setItemVariant(existing);
        Long qLineCount=quotationLineService.getCountQuotationLineList(quotationLine);

        RequestForQuotationLine rfqLine=new RequestForQuotationLine();
        rfqLine.setItemVariant(existing);
        Long rfqCount=requestForQuotationService.getCountRequestForQuotationLineList(rfqLine);

        if (prLineCount > 0) {

            throw new ResourceAlreadyUsedException("Cannot delete Item Variant because it is used in Purchase Requests");
        }

        if (rfqCount>0) {

            throw new ResourceAlreadyUsedException("Cannot delete Item Variant because it is used in RFQs");
        }

        if (qLineCount>0) {

            throw new ResourceAlreadyUsedException("Cannot delete Item Variant because it is used in Quotations");
        }

        if (poLineCount>0) {

            throw new ResourceAlreadyUsedException("Cannot delete Item Variant because it is used in Purchase Orders");
        }

        itemVariantRepository.deleteById(id);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.ITEM_VARIANT);
        log.setEntityId(id);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());

        auditLogsService.addAuditLog(log);
    }

    public List<ItemVariant> getItemVariantsList(ItemVariant itemVariant) {

        Specification<ItemVariant> spec = Specification
                .where(ItemVariantSpecification.hasId(itemVariant.getId()))
                .and(ItemVariantSpecification.hasItem(itemVariant.getItem()))
                .and(ItemVariantSpecification.hasSpecification(itemVariant.getSpecification()))
                .and(ItemVariantSpecification.hasActive(itemVariant.getActive()));


        return itemVariantRepository.findAll(spec);
    }

    public Long getCountItemVariants(ItemVariant itemVariant) {

        Specification<ItemVariant> spec = Specification
                .where(ItemVariantSpecification.hasId(itemVariant.getId()))
                .and(ItemVariantSpecification.hasItem(itemVariant.getItem()))
                .and(ItemVariantSpecification.hasSpecification(itemVariant.getSpecification()))
                .and(ItemVariantSpecification.hasActive(itemVariant.getActive()));


        return itemVariantRepository.count(spec);
    }


}