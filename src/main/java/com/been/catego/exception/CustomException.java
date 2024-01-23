package com.been.catego.exception;

public class CustomException extends RuntimeException {

    public CustomException(ErrorMessages errorMessages) {
        super(errorMessages.getMessage());
    }

    public CustomException(String message) {
        super(message);
    }
}
