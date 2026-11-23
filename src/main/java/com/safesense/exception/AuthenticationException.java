package com.safesense.exception;

/**
 * OOP: Exception handling — thrown when login credentials are wrong.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
