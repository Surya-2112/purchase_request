package com.module.purchase.customException;

public class ResourceAlreadyUsedException extends RuntimeException {
    
    public ResourceAlreadyUsedException(String message) {
        super(message);
    }
    
}
