package com.module.purchase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

@Embeddable
public class Address {
    
    @Column(name = "address_line") 
    private String addressLine;

    @Column(name = "street") 
    private String street;

    @Column(name = "city")
    @Size(max = 150)
    private String city;

    @Column(name = "state")
    @Size(max = 100)
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    @Size(max = 50)
    private String country;

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
