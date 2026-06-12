package com.module.purchase.entity;

import java.time.LocalDate;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

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
import jakarta.persistence.OneToMany;
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
    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @NotNull
    @Column(name = "request_end_date", nullable = false)
    private LocalDate requestEndDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status", nullable = false)
    @ColumnDefault("'DRAFT'")
    private RequestForQuotationStatus status = RequestForQuotationStatus.DRAFT;

    @OneToMany(mappedBy = "requestForQuotation")
    private Set<RequestForQuotationLine> requestForQuotationLines;

    @OneToMany(mappedBy = "requestForQuotation")
    private Set<Quotation> quotations; 

    // @NotNull
    // @ManyToOne
    // @JoinColumn(name = "category_id",nullable = false)
    // private Category category;

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

    public Set<RequestForQuotationLine> getRequestForQuotationLines() {
        return requestForQuotationLines;
    }

    public void setRequestForQuotationLines(Set<RequestForQuotationLine> requestForQuotationLines) {
        this.requestForQuotationLines = requestForQuotationLines;
    }

    public Set<Quotation> getQuotations() {
        return quotations;
    }

    public void setQuotations(Set<Quotation> quotations) {
        this.quotations = quotations;
    }

    // public Category getCategory() {
    //     return category;
    // }

    // public void setCategory(Category category) {
    //     this.category = category;
    // }
}