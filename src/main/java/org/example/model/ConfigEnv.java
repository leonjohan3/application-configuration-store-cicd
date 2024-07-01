package org.example.model;

import static org.apache.commons.lang3.Validate.matchesPattern;
import static org.example.constants.ServiceConstants.APP_AND_ENV_NAME_MESSAGE;
import static org.example.constants.ServiceConstants.APP_AND_ENV_NAME_PATTERN;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ConfigEnv(String name) {

    public ConfigEnv {
        matchesPattern(name, APP_AND_ENV_NAME_PATTERN, "invalid environment name: " + APP_AND_ENV_NAME_MESSAGE);
    }
}
