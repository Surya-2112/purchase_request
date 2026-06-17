package com.module.purchase.entity;

import java.time.LocalDate;

import com.module.purchase.enums.RequestForQuotationStatus;

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
@Table(name = "request_for_quotation")
public class RequestForQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_for_quotation_id")
    private Long id;

    @NotNull
    @Column(name = "requested_date")
    private LocalDate requestedDate;

    @NotNull
    @Column(name = "request_end_date")
    private LocalDate requestEndDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status")
    private RequestForQuotationStatus status = RequestForQuotationStatus.DRAFT;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDate getRequestEndDate() {
        return requestEndDate;
    }

    public void setRequestEndDate(LocalDate requestEndDate) {
        this.requestEndDate = requestEndDate;
    }

    public RequestForQuotationStatus getStatus() {
        return status;
    }

    public void setStatus(RequestForQuotationStatus status) {
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}