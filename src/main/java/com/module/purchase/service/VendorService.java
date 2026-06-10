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

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Category;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Vendor;
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

    public Vendor saveVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public Vendor addVendor(Vendor vendor,Employee employee) {
        Optional<Vendor> existingVendor = vendorRepository.findByVendorEmail(vendor.getVendorEmail());
        if (existingVendor.isPresent()) {
            throw new ResourceAlreadyUsedException("Vendor already exists with email: " + vendor.getVendorEmail());
        }

        vendor = saveVendor(vendor);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.VENDOR);
        log.setEntityId(vendor.getVendorId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

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

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.VENDOR);
        log.setEntityId(vendor.getVendorId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return vendor;
    }

    public void deleteVendorById(Long vendorId,Employee employee) {
        Vendor existingVendor = getVendorById(vendorId).get();
        if (existingVendor.getPurchaseOrderHeader() != null && !existingVendor.getPurchaseOrderHeader().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete vendor with associated purchase order header");
        }
        vendorRepository.deleteById(vendorId);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.VENDOR);
        log.setEntityId(vendorId);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }
}
