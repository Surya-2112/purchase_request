package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Category;

public class CategorySpecification {
    
     public static Specification<Category> hasCategoryId(Long categoryId)
    {
        return (root,query,cb)->
         categoryId == null? null
         : cb.equal(root.get("categoryId"),categoryId);
    }

    public static Specification<Category> hasCategoryName(String categoryName)
    {
        return (root,query,cb) ->
        categoryName == null || categoryName.isEmpty()
        ? null
        : cb.like(cb.lower(root.get("categoryName")),"%"+categoryName.toLowerCase()+"%"); 
    }
}
