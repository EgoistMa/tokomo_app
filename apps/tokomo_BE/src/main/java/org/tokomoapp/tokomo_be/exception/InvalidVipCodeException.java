package org.tokomoapp.tokomo_be.exception;

public class InvalidVipCodeException extends RuntimeException {
    public InvalidVipCodeException(String message) {
        super(message);
    }
} 