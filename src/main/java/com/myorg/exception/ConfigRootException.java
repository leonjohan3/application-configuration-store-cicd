package com.myorg.exception;

public class ConfigRootException extends RuntimeException {

    public ConfigRootException(final String errorMessage) {
        super(errorMessage);
    }
}
