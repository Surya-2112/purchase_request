package com.module.purchase.entity;

import java.time.LocalDate;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import com.module.purchase.enums.RequestForQuotationStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "request_for_quotation")

public class RequestForQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_for_quotation_id")
    private Long id;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(name = "request_end_date", nullable = false)
    private LocalDate requestEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("DRAFT")
    private RequestForQuotationStatus status = RequestForQuotationStatus.DRAFT;

    @OneToMany(mappedBy = "requestForQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RequestForQuotationLine> requestForQuotationLines;

    @OneToMany(mappedBy = "requestForQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Quotation> quotations;

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
}