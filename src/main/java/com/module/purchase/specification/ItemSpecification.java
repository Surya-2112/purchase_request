package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Category;
import com.module.purchase.entity.Item;
import com.module.purchase.entity.Unit;

public class ItemSpecification {

    public static Specification<Item> hasItemId(Long itemId) {
        return (root, query, cb)
                -> itemId == null ? null
                        : cb.equal(root.get("itemId"), itemId);
    }

    public static Specification<Item> hasItemCode(String itemCode) {
        return (root, query, cb)
                -> itemCode == null || itemCode.isEmpty() ? null
                : cb.like(cb.lower(root.get("itemCode")), "%" + itemCode.toLowerCase() + "%");
    }

    public static Specification<Item> hasItemName(String itemName) {
        return (root, query, cb)
                -> itemName == null || itemName.isEmpty() ? null
                : cb.like(cb.lower(root.get("itemName")), "%" + itemName.toLowerCase() + "%");
    }

    public static Specification<Item> hasCategory(Category category) {
        return (root, query, cb)
                -> category == null
                        ? null
                        : cb.equal(root.get("category"), category);
    }

    public static Specification<Item> hasUnit(Unit unit) {
        return (root, query, cb)
                -> unit == null
                        ? null
                        : cb.equal(root.get("unit"), unit);
    }
}
