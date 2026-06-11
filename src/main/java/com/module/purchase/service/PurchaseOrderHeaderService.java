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

import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.DiscountType;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Status;
import com.module.purchase.mapper.PurchaseOrderMapper;
import com.module.purchase.repository.PurchaseOrderHeaderRepository;
import com.module.purchase.specification.PurchaseOrderSpecification;

@Service
public class PurchaseOrderHeaderService {
    
    @Autowired
    private PurchaseOrderHeaderRepository purchaseOrderHeaderRepository;

    @Autowired
    private PurchaseOrderLineService  purchaseOrderLineService;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Lazy
    @Autowired
    private QuotationService quotationService;

    @Autowired 
    private DepartmentBudgetService departmentBudgetService;

    @Autowired
    private AuditLogsService auditLogsService;

    public PurchaseOrderHeader savePurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader) {
        return purchaseOrderHeaderRepository.save(purchaseOrderHeader);
    }

    public PurchaseOrderHeader addPurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader,Employee employee) {
             
        purchaseOrderHeader=savePurchaseOrderHeader(purchaseOrderHeader);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_ORDER);
        log.setEntityId(purchaseOrderHeader.getPurchaseOrderId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseOrderHeader;
    }

     public Long countByStatus(Status status)
    {
        return purchaseOrderHeaderRepository.countByStatus(status);
    }

    public Optional<PurchaseOrderHeader> getPurchaseOrderHeaderById(Long id) {
        Optional<PurchaseOrderHeader>  existingPurchaseOrderHeader = purchaseOrderHeaderRepository.findById(id);
        if (!existingPurchaseOrderHeader.isPresent()) {
            throw new ResourceNotFoundException("Purchase order header not found with id: " + id);
        }
        return existingPurchaseOrderHeader;
    }

    public List<PurchaseOrderDTO> getAllPurchaseOrdersfilter(PurchaseOrderDTO purchaseOrderDTO)
    {
         Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasCreatedBy(purchaseOrderDTO.getCreatedBy()))
                .and(PurchaseOrderSpecification.hasDepartment(purchaseOrderDTO.getForDepartment()))
                .and(PurchaseOrderSpecification.hasStatus(purchaseOrderDTO.getStatus()));

            return purchaseOrderMapper.toPurchaseOrdersDTO(purchaseOrderHeaderRepository.findAll(spec));
    }

    public List<PurchaseOrderDTO> getRecentPurchaseOrders(PageRequest pageRequest) {
        return purchaseOrderMapper.toPurchaseOrdersDTO(purchaseOrderHeaderRepository.findAllByOrderByPurchaseOrderIdDesc(pageRequest));
    }


     public Page<PurchaseOrderDTO> getAllPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO, int page, int size) {

        Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasVendor(purchaseOrderDTO.getVendor()));

        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrderHeader> prpage = purchaseOrderHeaderRepository.findAll(spec, pageable);
        return prpage.map(purchaseOrderMapper::toPurchaseOrderDTO);
    }

    public void genratePurchaseOrder(Quotation quotation)
    {   
        PurchaseOrderHeader purchaseOrderHeader=new PurchaseOrderHeader();
        purchaseOrderHeader.setQuotation(quotation);
        purchaseOrderHeader.setStatus(Status.DRAFT);
        purchaseOrderHeader.setTotalAmount(quotation.getTotalAmount());
        purchaseOrderHeader.setCreatedDate(LocalDate.now());
        purchaseOrderHeader.setVendor(quotation.getVendor());
        purchaseOrderHeader.setCreatedBy(null);

        addPurchaseOrderHeader(purchaseOrderHeader,null);
        List<QuotationLine> lines= quotationService.getLinesByQuotation(quotation);
        for(QuotationLine line :lines)
        {    PurchaseOrderLine poline=new PurchaseOrderLine();
            poline.setItemVariant(line.getItemVariant());
            poline.setPurchaseOrderHeader(purchaseOrderHeader);
            poline.setUnitPrice(line.getUnitPrice());
            poline.setQuantity(line.getRequestForQuotationLine().getRequestedQuantity());
             Double maxDiscount=0.0;
            for(DiscountType discounts:quotationService.getDiscountsByLine(line))
            {
                if(discounts.getFromQuantity()<=poline.getQuantity() && discounts.getToQuantity() >= poline.getQuantity())
                {
                    maxDiscount=discounts.getDiscountPercentage();
                }
            }
            if(maxDiscount==0.0) 
            {
                poline.setDiscountAmount(0.0);
            }
            else{
            poline.setDiscountAmount(poline.getUnitPrice()/(maxDiscount)); }
            poline.setTotalAmount((poline.getUnitPrice()*poline.getQuantity())-poline.getDiscountAmount());
            purchaseOrderLineService.addPurchaseOrderLine(poline);
        }
    }
}
