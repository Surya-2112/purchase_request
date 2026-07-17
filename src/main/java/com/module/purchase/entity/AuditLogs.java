package com.module.purchase.entity;

import java.time.OffsetDateTime;

import com.module.purchase.enums.Action;
import com.module.purchase.enums.EntityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "audit_logs")
public class AuditLogs {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long auditLogId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "entity_type")
    private EntityType entityType;

    @NotNull
    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "performed_action")
    private Action action;

    @ManyToOne
    @JoinColumn(name = "performed_by_id", referencedColumnName = "employee_id")
    private Employee performedBy;

    @NotNull
    @Column(name = "log_timestamp", nullable = false)
    private OffsetDateTime  timestamp;

    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Employee getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Employee performedBy) {
        this.performedBy = performedBy;
    }

    public OffsetDateTime  getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime  timestamp) {
        this.timestamp = timestamp;
    }
}