package com.module.purchase.service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.DepartmentBudget;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
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

    @Autowired
    private PurchaseRequestLineService  purchaseRequestLineService;

    @Autowired 
    private DepartmentBudgetService departmentBudgetService;

    @Autowired
    private UsersService userservice;

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

    public Optional<PurchaseOrderHeader> getPurchaseOrderHeaderById(Long id) {
        Optional<PurchaseOrderHeader>  existingPurchaseOrderHeader = purchaseOrderHeaderRepository.findById(id);
        if (!existingPurchaseOrderHeader.isPresent()) {
            throw new RuntimeException("Purchase order header not found with id: " + id);
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

     public List<PurchaseOrderDTO> getPurchaseOrderHeaders() {
        return purchaseOrderMapper.toPurchaseOrdersDTO(purchaseOrderHeaderRepository.findAll());
    }

    public List<PurchaseOrderHeader> getAllPurchaseOrderHeaders() {
        return purchaseOrderHeaderRepository.findAll();
    }

    public List<PurchaseOrderDTO> getRecentPurchaseOrders(PageRequest pageRequest) {
        return purchaseOrderMapper.toPurchaseOrdersDTO(purchaseOrderHeaderRepository.findAllByOrderByPurchaseOrderIdDesc(pageRequest));
    }


    public Page<PurchaseOrderDTO> getCreatedByUser(PurchaseOrderDTO purchaseOrderDTO, Long userId, int page,
            int size) {
        Employee existEmployee = userservice.getUserById(userId).get().getEmployee();
        Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasCreatedBy(existEmployee))
                .and(PurchaseOrderSpecification.hasDepartment(purchaseOrderDTO.getForDepartment()))
                .and(PurchaseOrderSpecification.hasStatus(purchaseOrderDTO.getStatus()));

        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrderHeader> prpage = purchaseOrderHeaderRepository.findAll(spec, pageable);
        return prpage.map(purchaseOrderMapper::toPurchaseOrderDTO);
    }

     public Page<PurchaseOrderDTO> getAllPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO, int page, int size) {

        Specification<PurchaseOrderHeader> spec = Specification
                .where(PurchaseOrderSpecification.hasPurchaseOrderId(purchaseOrderDTO.getPurchaseOrderId()))
                .and(PurchaseOrderSpecification.hasCreatedBy(purchaseOrderDTO.getCreatedBy()))
                .and(PurchaseOrderSpecification.hasDepartment(purchaseOrderDTO.getForDepartment()))
                .and(PurchaseOrderSpecification.hasStatus(purchaseOrderDTO.getStatus()));

        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrderHeader> prpage = purchaseOrderHeaderRepository.findAll(spec, pageable);
        return prpage.map(purchaseOrderMapper::toPurchaseOrderDTO);
    }


    public PurchaseOrderHeader updatePurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader,Employee employee)
    {
       if(purchaseOrderHeader.getStatus()==Status.CANCELLED)
       {  DepartmentBudget departmentBudget=departmentBudgetService.getByDepartmentAndYear(purchaseOrderHeader.getPurchaseRequestHeader().getForDepartment(), Year.now());
        departmentBudget.setRemainingBudgetAmount(departmentBudget.getRemainingBudgetAmount()+purchaseOrderHeader.getPurchaseRequestHeader().getTotalAmount());
        departmentBudgetService.updateDepartmentBudget(departmentBudget,employee);
       }

       purchaseOrderHeader=savePurchaseOrderHeader(purchaseOrderHeader);
        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.PURCHASE_ORDER);
        log.setEntityId(purchaseOrderHeader.getPurchaseOrderId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return purchaseOrderHeader;
    }

    public Long countAll()
    {
        return purchaseOrderHeaderRepository.count();
    }

    public Long countByStatus(Status status)
    {
        return purchaseOrderHeaderRepository.countByStatus(status);
    }

    public void genratepurchaseOrder(PurchaseRequestHeader purchaseRequestHeader)
    {   
        PurchaseOrderHeader purchaseOrderHeader=new PurchaseOrderHeader();
        purchaseOrderHeader.setPurchaseRequestHeader(purchaseRequestHeader);
        purchaseOrderHeader.setStatus(Status.ORDER);
        purchaseOrderHeader.setTotalAmount(purchaseRequestHeader.getTotalAmount());
        purchaseOrderHeader.setCreatedDate(LocalDate.now());
        purchaseOrderHeader.setVendor(purchaseRequestHeader.getVendor());

        DepartmentBudget departmentBudget=departmentBudgetService.getByDepartmentAndYear(purchaseRequestHeader.getForDepartment(), Year.now());
        departmentBudget.setRemainingBudgetAmount(departmentBudget.getRemainingBudgetAmount()-purchaseRequestHeader.getTotalAmount());
        departmentBudgetService.updateDepartmentBudget(departmentBudget,null);

        purchaseOrderHeader = addPurchaseOrderHeader(purchaseOrderHeader,null);
        List<PurchaseRequestLine> lines= purchaseRequestLineService.getPurchaseRequestLineByHeader(purchaseRequestHeader);
        for(PurchaseRequestLine line :lines)
        {    PurchaseOrderLine poline=new PurchaseOrderLine();
             poline.setItem(line.getItem());
             poline.setPurchaseOrderHeader(purchaseOrderHeader);
             poline.setQuantity(line.getQuantity());
             poline.setUnitPrice(line.getUnitPrice());
             poline.setTotalPrice(line.getTotalPrice());
             poline.setDiscount(line.getDiscount()==null? 0:line.getDiscount());
            purchaseOrderLineService.addPurchaseOrderLine(poline);
        }
    }
}
