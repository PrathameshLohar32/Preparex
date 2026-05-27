package com.preparex.preparex_backend.exception;

public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String field, String value) {
        super(field + " already exists: " + value, "DUPLICATE_RESOURCE");
    }
}
