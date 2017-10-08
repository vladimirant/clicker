package com.clicker.exception;

/**
 * Created by vladimir on 27.09.15.
 */
public class RegistrationException extends Exception {

    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistrationException(Throwable cause) {
        super(cause);
    }
}
