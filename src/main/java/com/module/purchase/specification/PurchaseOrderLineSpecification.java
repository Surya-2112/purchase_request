package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.PurchaseOrderHeader;
import com.module.purchase.entity.PurchaseOrderLine;

public class PurchaseOrderLineSpecification {
    
    public static Specification<PurchaseOrderLine> hasId(Long id)
    {
        return (root,query,cb)->
            id == null? null
            : cb.equal(root.get("id"),id);
    }

    public static Specification<PurchaseOrderLine> hasPurchaseOrderHeader(PurchaseOrderHeader purchaseOrderHeader)
    {
        return (root,query,cb)->
            purchaseOrderHeader == null? null
            : cb.equal(root.get("purchaseOrderHeader"),purchaseOrderHeader);
    }

    public static Specification<PurchaseOrderLine> hasItemVariant(ItemVariant itemVariant)
    {
        return (root,query,cb)->
            itemVariant == null? null
            : cb.equal(root.get("itemVariant"),itemVariant);
    }

}
