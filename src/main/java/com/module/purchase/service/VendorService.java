package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.PurchaseOrderDTO;
import com.module.purchase.entityDTO.VendorDTO;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;
import com.module.purchase.mapper.VendorMapper;
import com.module.purchase.repository.VendorRepository;
import com.module.purchase.specification.VendorSpecification;

@Service
@Transactional
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private AuditLogsService auditLogsService;

    @Autowired
    private PurchaseOrderHeaderService purchaseOrderService;

    @Autowired 
    @Lazy
    private UsersService userService;

    public Vendor saveVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public Vendor addVendor(Vendor vendor,Employee employee) {
        Optional<Vendor> existingVendor = vendorRepository.findByVendorEmail(vendor.getVendorEmail());
        if (existingVendor.isPresent()) {
            throw new ResourceAlreadyUsedException("Vendor already exists with email: " + vendor.getVendorEmail());
        }
        vendor = saveVendor(vendor);
       auditLogsService.addAuditLog(EntityType.VENDOR, vendor.getVendorId(), Action.CREATE, employee);

        return vendor;
    }

    public Optional<Vendor> getVendorById(Long id) {
        Optional<Vendor> existingVendor = vendorRepository.findById(id);
        if (!existingVendor.isPresent()) {
            throw new ResourceNotFoundException("Vendor not found with id: " + id);
        }
        return existingVendor;
    }

    public List<Vendor> getVendors() {
        return vendorRepository.findAll();
    }

    public List<Vendor> getVendorsByCategory(Category category)
    {
        return vendorRepository.findByCategoriesContaining(category);
    }

    public List<Vendor> getVendorsWithoutUser()
    {
        return vendorRepository.findVendorsWithoutUser();
    }

    public Page<VendorDTO> getAllVendors(VendorDTO vendorDTO, int page, int size) {

        Specification<Vendor> spec = Specification
                .where(VendorSpecification.hasVendorId(vendorDTO.getVendorId()))
                .and(VendorSpecification.hasVendorName(vendorDTO.getVendorName()))
                .and(VendorSpecification.hasActive(vendorDTO.getActive()));
        Pageable pageable = PageRequest.of(page, size);
        Page<Vendor> vendorPage = vendorRepository.findAll(spec, pageable);
        return vendorPage.map(vendorMapper::toVendorDTO);
    }

    public Vendor updateVendor(Vendor vendor,Employee employee) {
        Vendor existingVendor = getVendorById(vendor.getVendorId()).get();
        if (!existingVendor.getVendorEmail().equals(vendor.getVendorEmail())) {
            throw new ModificationNotAllowedException("Cannot update vendor email ");
        }
        vendor = saveVendor(vendor);
        if(vendor.getActive()==false && vendor.getUsers()!=null)
        {   vendor.getUsers().setActive(false);
            userService.updateUser(vendor.getUsers(), employee);
        }
       auditLogsService.addAuditLog(EntityType.VENDOR, vendor.getVendorId(), Action.UPDATE, employee);

        return vendor;
    }

    public void deleteVendorById(Long vendorId,Employee employee) {
        Vendor existingVendor = getVendorById(vendorId).get();
        PurchaseOrderDTO purchaseOrderDTO= new PurchaseOrderDTO();
        purchaseOrderDTO.setVendor(existingVendor);
        if (purchaseOrderService.getCountPurchaseOrder(purchaseOrderDTO)>0) {
            throw new ResourceAlreadyUsedException("Cannot delete vendor with associated purchase order header");
        }
        vendorRepository.deleteById(vendorId);

       auditLogsService.addAuditLog(EntityType.VENDOR, vendorId, Action.DELETE, employee);
    }
}
