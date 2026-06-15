package com.module.purchase.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.module.purchase.entity.PurchaseRequestDocument;
import com.module.purchase.entity.PurchaseRequestHeader;
import com.module.purchase.repository.PurchaseRequestDocumentRepositor;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PurchaseRequestDocumentService {

    @Autowired
    private  PurchaseRequestDocumentRepositor repository;

    public PurchaseRequestDocument save(PurchaseRequestDocument document) {

        return repository.save(document);
    }

    public List<PurchaseRequestDocument> getAll() {

        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PurchaseRequestDocument> getByPurchaseRequestHeader(PurchaseRequestHeader purchaseRequestHeader)
    {
         List<PurchaseRequestDocument> docs = repository.findAllByPurchaseRequestHeader(purchaseRequestHeader);

        for (PurchaseRequestDocument doc : docs) {
            if (doc.getDocumentData() != null) {
                int length = doc.getDocumentData().length; 
            }
        }
        return docs;
    }

    public void delete(PurchaseRequestDocument document)
    {
        repository.delete(document);
    }
}