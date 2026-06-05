package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Category;
import com.module.purchase.repository.CategoryRepository;
import com.module.purchase.specification.CategorySpecification;

import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
 private AuditLogsService auditLogsService;


    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category addCategory(Category category,Employee employee) {
        
        Optional<Category> existingCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if (existingCategory.isPresent()) {
            throw new ResourceAlreadyUsedException("Category already exists with name: " + category.getCategoryName());
        }
        category = saveCategory(category);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.CATEGORY);
        log.setEntityId(category.getCategoryId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return category;
    }

    public Optional<Category> getCategoryById(Long id) {
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if (!existingCategory.isPresent()) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        return existingCategory;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public Page<Category> getAllCategories(Category category,int page,int size)
    {  
        Specification<Category> spec= Specification
        .where(CategorySpecification.hasCategoryId(category.getCategoryId()))
        .and(CategorySpecification.hasCategoryName(category.getCategoryName()));

        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);
        return categoryPage;
    }

    public Category updateCategory(Category category,Employee employee)
    {    
        getCategoryById(category.getCategoryId()).get();
        category=saveCategory(category);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.CATEGORY);
        log.setEntityId(category.getCategoryId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return category;
    }

    public void deleteCategoryById(Long categoryId,Employee employee)
    {  Category  existingCategory =  getCategoryById(categoryId).get();
        // if(existingCategory.getVendors()!=null && !existingCategory.getVendors().isEmpty())
        // {
        //      throw new ResourceAlreadyUsedException("Cannot delete vendor category with associated vendors");
        // }
        
        categoryRepository.deleteById(categoryId);

        AuditLogs log = new AuditLogs();
        log.setEntityType(EntityType.CATEGORY);
        log.setEntityId(categoryId);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }
}
