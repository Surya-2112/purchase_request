package com.module.purchase.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.entity.Vendor;
import com.module.purchase.entityDTO.VendorDTO;
import com.module.purchase.mapper.VendorMapper;
import com.module.purchase.repository.VendorRepository;
import com.module.purchase.specification.VendorSpecification;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class VendorService {
    
    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorMapper vendorMapper;

    public Vendor saveVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public Vendor addVendor(Vendor vendor) {
        Optional<Vendor> existingVendor = vendorRepository.findByVendorEmail(vendor.getVendorEmail());
        if (existingVendor.isPresent()) {
            throw new RuntimeException("Vendor already exists with email: " + vendor.getVendorEmail());
        }
        return saveVendor(vendor);
    }

    public Optional<Vendor> getVendorById(Long id) {
        Optional<Vendor> existingVendor = vendorRepository.findById(id);
        if (!existingVendor.isPresent()) {
            throw new RuntimeException("Vendor not found with id: " + id);
        }
        return existingVendor;
    }

    public List<Vendor> getVendors() {
        return vendorRepository.findAll();
    }

   public Page<VendorDTO> getAllVendors(VendorDTO vendorDTO, int page, int size) {

        Specification<Vendor> spec = Specification
                .where(VendorSpecification.hasVendorId(vendorDTO.getVendorId()))
                .and(VendorSpecification.hasVendorName(vendorDTO.getVendorName()))
                .and(VendorSpecification.hasVendorCategory(vendorDTO.getVendorCategory()))
                .and(VendorSpecification.hasActive(vendorDTO.getActive()));
        Pageable pageable = PageRequest.of(page, size);
        Page<Vendor> vendorPage = vendorRepository.findAll(spec, pageable);
        return vendorPage.map(vendorMapper::toVendorDTO);
    }

    public Vendor updateVendor(Vendor vendor)
    { 
        Vendor existingVendor=getVendorById(vendor.getVendorId()).get();
        if(!existingVendor.getVendorEmail().equals(vendor.getVendorEmail()))
        {
            throw new ModificationNotAllowedException("Cannot update vendor email ");
        }
        return saveVendor(vendor);
    }

    public void deleteVendorById(Long vendorId)
    {  
        Vendor existingVendor=getVendorById(vendorId).get();
        if(existingVendor.getPurchaseOrderHeader()!=null && !existingVendor.getPurchaseOrderHeader().isEmpty())
        {
            throw new ResourceAlreadyUsedException("Cannot delete vendor with associated purchase order header");
        }
        vendorRepository.deleteById(vendorId);
    }
}
