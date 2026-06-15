package com.module.purchase.service;

import java.util.ArrayList;
import java.util.List;
import com.module.purchase.entity.Needs;
import com.module.purchase.enums.EntityType;
import com.module.purchase.repository.NeedsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class NeedsService {

    @Autowired
    private NeedsRepository needsRepository;

    public List<Needs> getAllNeeds() {
        return needsRepository.findAll();
    }

    public List<Needs> getNeedsByDomain(EntityType entityType) {
        if (entityType == null) {
            return List.of();
        }
        return needsRepository.findByEntityType(entityType);
    }

    public List<Needs> getSpecificNeedRecord(EntityType entityType, Long refId) {
        if (entityType == null || refId == null) {
            return new ArrayList();
        }
        return needsRepository.findByEntityTypeAndRefId(entityType, refId);
    }

    @Transactional
    public Needs registerNewCatalogNeed(String rawNeedText, EntityType entityType, Long sourceLineId) {
        if (rawNeedText == null || rawNeedText.isEmpty() || entityType == null || sourceLineId == null) {
            throw new IllegalArgumentException(
                    "Compliance Fault: All parameters are required need entry.");
        }

        Needs needRecord = new Needs();
        needRecord.setNeedLine(rawNeedText.trim());
        needRecord.setEntityType(entityType);
        needRecord.setRefId(sourceLineId);
        return needsRepository.save(needRecord);
    }

    @Transactional
    public void resolveAndClearCompletedNeed(EntityType entityType, Long Id) {
        if (entityType == null || Id == null) {
            return;
        }
        needsRepository.deleteByEntityTypeAndId(entityType, Id);
    }

}