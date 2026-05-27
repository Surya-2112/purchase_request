package com.module.purchase.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.repository.PurchaseRequestDocumentRepositor;

@Service
public class PurchaseRequestDocumentService {

    @Autowired
    private  PurchaseRequestDocumentRepositor repository;

    public PurchaseRequestDocument save(PurchaseRequestDocument document) {

        return repository.save(document);
    }

    public List<PurchaseRequestDocument> getAll() {

        return repository.findAll();
    }

    public List<PurchaseRequestDocument> getByPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader)
    {
        return repository.findAllByPurchaseRequestHeader(purchaseRequestHeader);
    }

    public void delete(PurchaseRequestDocument document)
    {
        repository.delete(document);
    }
}