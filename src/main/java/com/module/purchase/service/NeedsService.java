package com.module.purchase.service;

import java.util.List;
import java.util.Optional;
import com.module.purchase.entity.Needs;
import com.module.purchase.entity.PurchaseRequestLine;
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

    public Optional<Needs> getSpecificNeedRecord(EntityType entityType, Long refId) {
        if (entityType == null || refId == null) {
            return Optional.empty();
        }
        return needsRepository.findByEntityTypeAndRefId(entityType, refId);
    }

    @Transactional
    public Needs registerNewCatalogNeed(String rawNeedText, EntityType entityType, Long sourceLineId) {
        if (rawNeedText == null || rawNeedText.isEmpty() || entityType == null || sourceLineId == null) {
            throw new IllegalArgumentException(
                    "Compliance Fault: All parameters are required to log an ad-hoc need entry.");
        }

        needsRepository.findByEntityTypeAndRefId(entityType, sourceLineId)
                .ifPresent(needsRepository::delete);

        Needs needRecord = new Needs();
        needRecord.setNeedLine(rawNeedText.trim());
        needRecord.setEntityType(entityType);
        needRecord.setRefId(sourceLineId);

        return needsRepository.save(needRecord);
    }

    @Transactional
    public void resolveAndClearCompletedNeed(EntityType entityType, Long refId) {
        if (entityType == null || refId == null) {
            return;
        }
        needsRepository.deleteByEntityTypeAndRefId(entityType, refId);
    }

}