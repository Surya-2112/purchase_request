package com.module.purchase.customException;

public class ResourceIsNotActiveException extends RuntimeException {

    public ResourceIsNotActiveException(String message)
    {
        super(message);
    }
}
