package org.example.exception;

import java.io.Serializable;

public class ConfigRootException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -8824317522275841029L;

    public ConfigRootException(final String errorMessage) {
        super(errorMessage);
    }
}
