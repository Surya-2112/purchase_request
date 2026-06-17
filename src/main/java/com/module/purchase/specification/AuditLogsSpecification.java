package com.module.purchase.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;

public class AuditLogsSpecification {
    
    public static Specification<AuditLogs> hasAuditLogId(Long auditLogId)
    {
        return (root,query,cb)->
            auditLogId == null? null
            : cb.equal(root.get("auditLogId"),auditLogId);
    }

    public static Specification<AuditLogs> hasEntityType(EntityType entityType)
    {
        return (root,query,cb)->
            entityType == null? null
            : cb.equal(root.get("entityType"),entityType);
    }

    public static Specification<AuditLogs> hasEntityId(Long entityId)
    {
        return (root,query,cb)->
            entityId == null? null
            : cb.equal(root.get("entityId"),entityId);
    }

    public static Specification<AuditLogs> hasAction(Action action)
    {
        return (root,query,cb)->
            action == null? null
            : cb.equal(root.get("action"),action);
    }

    public static Specification<AuditLogs> hasPerformedBy(Employee performedBy )
    {
        return (root,query,cb)->
            performedBy == null? null
            : cb.equal(root.get("performedBy"),performedBy);
    }

    public static Specification<AuditLogs> hasTimestamp(LocalDate timestamp)
    {
        return (root,query,cb)->
            timestamp == null? null
            : cb.equal(root.get("timestamp"),timestamp);
    }
}
