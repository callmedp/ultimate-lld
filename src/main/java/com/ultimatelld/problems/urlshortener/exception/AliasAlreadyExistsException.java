package com.ultimatelld.problems.urlshortener.exception;

/** Thrown when a requested custom alias is already taken. */
public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String alias) {
        super("Alias already in use: " + alias);
    }
}
