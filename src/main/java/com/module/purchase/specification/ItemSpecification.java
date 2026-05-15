package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;
import com.module.purchase.entity.Item;

public class ItemSpecification {
    
    public static Specification<Item> hasItemId(Long itemId)
    {
        return (root,query,cb)->
            itemId == null ? null
            : cb.equal(root.get("itemId"),itemId);
    }

    public static Specification<Item> hasItemCode(String itemCode)
    {
        return (root,query,cb)->
            itemCode == null || itemCode.isEmpty() ? null
            : cb.like(root.get("itemCode"),"%"+itemCode+"%");
    }

    public static Specification<Item> hasItemName(String itemName)
    {
        return (root,query,cb)->
            itemName == null || itemName.isEmpty() ?null
            : cb.like(root.get("itemName"),"%"+itemName+"%"); 
    }
}
