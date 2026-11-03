package com.platform.common.exceptions.InventoryExceptions;

public class LockAcquisitionException extends RuntimeException{
    public LockAcquisitionException(String message){
        super(message);
    }
}
