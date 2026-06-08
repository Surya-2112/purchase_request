package com.module.purchase.specification;

import org.springframework.data.jpa.domain.Specification;
import com.module.purchase.entity.Unit;

public class UnitSpecification {

    public static Specification<Unit> hasId(Integer id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("id"), id);
    }

    public static Specification<Unit> hasName(String name) {
        return (root, query, cb) ->
                name == null || name.isEmpty()
                        ? null
                        : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Unit> hasCode(String code) {
        return (root, query, cb) ->
                code == null || code.isEmpty()
                        ? null
                        : cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%");
    }
}