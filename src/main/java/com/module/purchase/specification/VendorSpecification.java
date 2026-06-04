package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Vendor;
import com.module.purchase.entity.Category;


public class VendorSpecification {
    
    public static Specification<Vendor> hasVendorId(Long id)
    {
        return (root,query,cb)->
        id == null ? null
        : cb.equal(root.get("vendorId"),id);
    }

    public static Specification<Vendor> hasVendorName(String name)
    {
        return (root,query,cb)->
        name==null || name.isEmpty()
        ? null
        : cb.like(cb.lower(root.get("vendorName")),"%"+name.toLowerCase()+"%");
    }

    public static Specification<Vendor> hasActive(Boolean active) 
    {
        return (root,query,cb) ->
        active==null ?null
        :cb.equal(root.get("active"),active);
    }
}
