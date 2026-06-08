package com.module.purchase.entityDTO;

import java.util.List;
import com.module.purchase.entity.Category;

public class VendorDTO {
    
    private Long vendorId;

    private String vendorName;

    private Boolean active;

   private List<Category> categories;

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCategories(List<Category> categories)
    {
        this.categories=categories;
    } 

    public List<Category> getCategories()
    {  
        return categories;
    }

}
