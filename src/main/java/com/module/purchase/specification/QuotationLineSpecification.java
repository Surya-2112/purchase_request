package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.ItemVariant;
import com.module.purchase.entity.Quotation;
import com.module.purchase.entity.QuotationLine;
import com.module.purchase.entity.RequestForQuotationLine;

public class QuotationLineSpecification {
    
    public static Specification<QuotationLine> hasId(Long id)
    {
        return (root,query,cb)->
            id == null? null
            : cb.equal(root.get("id"),id);
    }

     public static Specification<QuotationLine> hasQuotation(Quotation quotation)
    {
        return (root,query,cb)->
            quotation == null? null
            : cb.equal(root.get("quotation"),quotation);
    }

    public static Specification<QuotationLine> hasItemVariant(ItemVariant itemVariant)
    {
        return (root,query,cb)->
            itemVariant == null? null
            : cb.equal(root.get("itemVariant"),itemVariant);
    }

    public static  Specification<QuotationLine> hasRequestForQuotationLine(RequestForQuotationLine requestForQuotationLine)
    {
        return (root,query,cb)-> 
            requestForQuotationLine==null? null
            : cb.equal(root.get("requestForQuotationLine"),requestForQuotationLine);
    }

}
