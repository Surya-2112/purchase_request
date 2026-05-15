package com.module.purchase.customException;

public class ResourceMissingFieldException extends RuntimeException{

    public ResourceMissingFieldException(String message)
    {
        super(message);
    }
}
