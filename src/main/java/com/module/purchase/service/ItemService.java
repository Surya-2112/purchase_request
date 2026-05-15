package com.module.purchase.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.module.purchase.customException.ModificationNotAllowedException;
import com.module.purchase.customException.ResourceAlreadyUsedException;
import com.module.purchase.entity.Item;
import com.module.purchase.repository.ItemRepository;
import com.module.purchase.specification.ItemSpecification;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ItemService {
    
    @Autowired
    private ItemRepository itemRepository;

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    public Item addItem(Item item) {
        Optional<Item> existingItem = itemRepository.findByItemCode(item.getItemCode());
        
        if (existingItem.isPresent()) { 
            throw new ResourceAlreadyUsedException("Item with code " + item.getItemCode() + " already exists");
        }
        return saveItem(item);
    }

    public Optional<Item> getItemById(Long id) {
        Optional<Item> existingItem = itemRepository.findById(id);
        if (!existingItem.isPresent()) {
            throw new RuntimeException("Item not found with id: " + id);
        }       
        return existingItem;
    }
    
    public Item updateItem(Item item)
    {   Item existingItem = getItemById(item.getItemId()).get();
        if(!existingItem.getItemCode().equals(item.getItemCode()))
        { throw new ModificationNotAllowedException("Cannot update item Code ");
        }
        return saveItem(item);
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

    public void deleteItemById(Long itemId) {

        Item existingItem = getItemById(itemId).get();
        if (existingItem.getPurchaseRequestLines() != null && !existingItem.getPurchaseRequestLines().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete item with associated purchase request lines");
        }
        if (existingItem.getPurchaseOrderLines() != null && !existingItem.getPurchaseOrderLines().isEmpty()) {
            throw new ResourceAlreadyUsedException("Cannot delete item with associated purchase order lines");
        }
        itemRepository.deleteById(itemId);
    }

}
