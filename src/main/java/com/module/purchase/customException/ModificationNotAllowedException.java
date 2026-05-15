package com.module.purchase.customException;

public class ModificationNotAllowedException extends RuntimeException {

    public ModificationNotAllowedException(String message)
    {
        super(message);
    }
}
