package com.preparex.preparex_backend.exception;

import lombok.Getter;

/**
 * Base class for all domain-specific exceptions in PreparEx.
 * All custom exceptions must extend this class.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;

    protected BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
