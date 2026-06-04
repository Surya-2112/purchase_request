package com.module.purchase.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.customException.ResourceNotFoundException;
import com.module.purchase.entity.AuditLogs;
import com.module.purchase.entity.Employee;
import com.module.purchase.entity.Item;
import com.module.purchase.repository.ItemRepository;
import com.module.purchase.specification.ItemSpecification;
import com.module.purchase.enums.EntityType;
import com.module.purchase.enums.Action;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ItemService {
    
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AuditLogsService auditLogsService;

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public Item addItem(Item item,Employee employee) {
        Optional<Item> existingItem = itemRepository.findByItemCode(item.getItemCode());
        
        if (existingItem.isPresent()) { 
            throw new ResourceAlreadyUsedException("Item with code " + item.getItemCode() + " already exists");
        }
        item=saveItem(item);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.EMPLOYEE);
        log.setEntityId(item.getItemId());
        log.setAction(Action.CREATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return item;
    }

    public Optional<Item> getItemById(Long id) {
        Optional<Item> existingItem = itemRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }       
        return existingItem;
    }
    
    public Item updateItem(Item item,Employee employee)
    {   Item existingItem = getItemById(item.getItemId()).get();
        if(!existingItem.getItemCode().equals(item.getItemCode()))
        { throw new ModificationNotAllowedException("Cannot update item Code ");}

        item=saveItem(item);

        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.EMPLOYEE);
        log.setEntityId(item.getItemId());
        log.setAction(Action.UPDATE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);

        return item;
    }

    public List<Item> getItems()
    {
        return itemRepository.findAll();
    }

    public Page<Item> getAllItems(Item item,int page,int size) {
        
        Specification<Item> spec= Specification
        .where(ItemSpecification.hasItemId(item.getItemId()))
        .and(ItemSpecification.hasItemCode(item.getItemCode()))
        .and(ItemSpecification.hasItemName(item.getItemName()));
        
        PageRequest pageable= PageRequest.of(page, size);

        Page<Item> pageItem= itemRepository.findAll(spec, pageable);

        return pageItem;
    }

    public void deleteItemById(Long itemId,Employee employee) {

        Item existingItem = getItemById(itemId).get();
        itemRepository.deleteById(itemId);
        
        AuditLogs log= new AuditLogs();
        log.setEntityType(EntityType.EMPLOYEE);
        log.setEntityId(itemId);
        log.setAction(Action.DELETE);
        log.setPerformedBy(employee);
        log.setTimestamp(LocalDate.now());
        auditLogsService.addAuditLog(log);
    }

}
