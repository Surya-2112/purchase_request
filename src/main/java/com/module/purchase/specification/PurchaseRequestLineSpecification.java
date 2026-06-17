package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.PurchaseOrderLine;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.entity.PurchaseRequestLine;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.enums.Status;

public class PurchaseRequestLineSpecification {
    
    public static Specification<PurchaseRequestLine> hasId(Long id)
    {
        return (root,query,cb)->
            id == null? null
            : cb.equal(root.get("id"),id);
    }

    public static Specification<PurchaseRequestLine> hasPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader)
    {
        return (root,query,cb)->
            purchaseRequestHeader == null? null
            : cb.equal(root.get("purchaseRequestHeader"),purchaseRequestHeader);
    }

    public static Specification<PurchaseRequestLine> hasItemVariant(ItemVariant itemVariant)
    {
        return (root,query,cb)->
            itemVariant == null? null
            : cb.equal(root.get("itemVariant"),itemVariant);
    }

    public static Specification<PurchaseRequestLine> hasStatus(Status status)
    {
        return (root,query,cb)->
            status == null? null
            : cb.equal(root.get("status"),status);
    }

    public static Specification<PurchaseRequestLine> hasRepeatableId(Long repeatableId)
    {
        return (root,query,cb)->
            repeatableId == null? null
            : cb.equal(root.get("repeatableId"),repeatableId);
    }

    public static Specification<PurchaseRequestLine> hasRequestForQuotation(RequestForQuotation requestForQuotation)
    {
        return (root,query,cb)->
            requestForQuotation == null? null
            : cb.equal(root.get("requestForQuotation"),requestForQuotation);
    }

     public static Specification<PurchaseRequestLine> hasPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)
    {
        return (root,query,cb)->
            purchaseOrderLine == null? null
            : cb.equal(root.get("purchaseOrderLine"),purchaseOrderLine);
    }
}
