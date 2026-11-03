package com.platform.common.exceptions.InventoryExceptions;

public class InsufficientInventoryException extends RuntimeException{

    public InsufficientInventoryException(String message){
        super(message);
    }

}
