package com.silverbars.exception;

/**
 * Checked Exception for Invalid Operation Performed within the Order Board application
 */
public class OrderBoardInvalidOperationException extends Exception {

    public OrderBoardInvalidOperationException(String errorMessage) {
        super(errorMessage);
    }
}
