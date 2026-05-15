package com.module.purchase.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.entity.VendorCategory;
import com.module.purchase.repository.VendorCategoryRepository;
import com.module.purchase.specification.VendorCategorySpecification;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class VendorCategoryService {
    
    @Autowired
    private VendorCategoryRepository vendorCategoryRepository;

    public VendorCategory saveVendorCategory(VendorCategory vendorCategory) {
        return vendorCategoryRepository.save(vendorCategory);
    }

    public VendorCategory addVendorCategory(VendorCategory vendorCategory) {
        
        Optional<VendorCategory> existingVendorCategory = vendorCategoryRepository.findByCategoryName(vendorCategory.getCategoryName());
        if (existingVendorCategory.isPresent()) {
            throw new RuntimeException("Vendor category already exists with name: " + vendorCategory.getCategoryName());
        }
        return saveVendorCategory(vendorCategory);
    }

    public Optional<VendorCategory> getVendorCategoryById(Long id) {
        Optional<VendorCategory> existingVendorCategory = vendorCategoryRepository.findById(id);
        if (!existingVendorCategory.isPresent()) {
            throw new RuntimeException("Vendor category not found with id: " + id);
        }
        return existingVendorCategory;
    }

    public List<VendorCategory> getVendorCategories() {
        return vendorCategoryRepository.findAll();
    }

    public Page<VendorCategory> getAllVendorCategories(VendorCategory vendorCategory,int page,int size)
    {  
        Specification<VendorCategory> spec= Specification
        .where(VendorCategorySpecification.hasCategoryId(vendorCategory.getCategoryId()))
        .and(VendorCategorySpecification.hasCategoryName(vendorCategory.getCategoryName()));

        Pageable pageable = PageRequest.of(page, size);
        Page<VendorCategory> vendorCategoryPage = vendorCategoryRepository.findAll(spec, pageable);
        return vendorCategoryPage;
    }

    public VendorCategory updateVendorCategory(VendorCategory vendorCategory)
    {    getVendorCategoryById(vendorCategory.getCategoryId()).get();
        return saveVendorCategory(vendorCategory);
    }

    public void deleteVendorCategoryById(Long categoryId)
    {  VendorCategory  existingCategory =  getVendorCategoryById(categoryId).get();
        if(existingCategory.getVendors()!=null && !existingCategory.getVendors().isEmpty())
        {
             throw new ResourceAlreadyUsedException("Cannot delete vendor category with associated vendors");
        }
        vendorCategoryRepository.deleteById(categoryId);
    }
}
