package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.Item;
import com.module.purchase.entity.ItemVariant;

public class ItemVariantSpecification {

    public static Specification<ItemVariant> hasId(Long id) {

        return (root, query, cb) ->
                id == null
                        ? null
                        : cb.equal(root.get("id"), id);
    }

    public static Specification<ItemVariant> hasItem(Item item) {

        return (root, query, cb) ->
                item == null
                        ? null
                        : cb.equal(root.get("item"), item);
    }

    public static Specification<ItemVariant> hasSpecification(String specification) {

        return (root, query, cb) ->
                specification == null || specification.trim().isEmpty()
                        ? null
                        : cb.like(
                                cb.lower(root.get("specification")),
                                "%" + specification.toLowerCase() + "%"
                        );
    }

    public static Specification<ItemVariant> hasActive(Boolean active) {

        return (root, query, cb) ->  active == null
                        ? null: cb.equal(root.get("active"), active);
    }

}