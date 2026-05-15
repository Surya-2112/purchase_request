package com.module.purchase.entityDTO;

import com.module.purchase.entity.VendorCategory;

public class VendorDTO {
    
    private Long vendorId;

    private String vendorName;

    private VendorCategory vendorCategory;

    private Boolean active;

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

    public VendorCategory getVendorCategory() {
        return vendorCategory;
    }

    public void setVendorCategory(VendorCategory vendorCategory) {
        this.vendorCategory = vendorCategory;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}
