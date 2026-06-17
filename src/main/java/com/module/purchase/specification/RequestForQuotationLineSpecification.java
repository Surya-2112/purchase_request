package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.RequestForQuotation;
import com.module.purchase.entity.RequestForQuotationLine;


public class RequestForQuotationLineSpecification {
    
     public static Specification<RequestForQuotationLine> hasId(Long id)
    {
        return (root,query,cb)->
            id == null? null
            : cb.equal(root.get("id"),id);
    }

     public static Specification<RequestForQuotationLine> hasRequestForQuotation(RequestForQuotation requestForQuotation)
    {
        return (root,query,cb)->
            requestForQuotation == null? null
            : cb.equal(root.get("requestForQuotation"),requestForQuotation);
    }

    public static Specification<RequestForQuotationLine> hasItemVariant(ItemVariant itemVariant)
    {
        return (root,query,cb)->
            itemVariant == null? null
            : cb.equal(root.get("itemVariant"),itemVariant);
    }
}
