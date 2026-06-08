package com.module.purchase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table; 
import jakarta.persistence.Lob;   

@Entity
@Table(name = "purchase_request_document") 
public class PurchaseRequestDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id") 
    private Long documentId;

    @Column(name = "file_name", nullable = false) 
    private String fileName;

    @Column(name = "file_type", nullable = false) 
    private String fileType;

    @Column(name = "file_size", nullable = false) 
    private Long fileSize;

    @Lob 
    @Column(name = "document_data")
    private byte[] documentData;

    @ManyToOne
    @JoinColumn(name = "purchase_request_id", referencedColumnName = "purchase_request_id") 
    private PurchaseRequestHeader purchaseRequestHeader;

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getDocumentData() {
        return documentData;
    }

    public void setDocumentData(byte[] documentData) {
        this.documentData = documentData;
    }

    public PurchaseRequestHeader getPurchaseRequestHeader() {
        return purchaseRequestHeader;
    }

    public void setPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader) {
        this.purchaseRequestHeader = purchaseRequestHeader;
    }

}