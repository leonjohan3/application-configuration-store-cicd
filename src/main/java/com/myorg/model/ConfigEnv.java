package com.myorg.model;

import static com.myorg.constants.ServiceConstants.APP_AND_ENV_NAME_MESSAGE;
import static com.myorg.constants.ServiceConstants.APP_AND_ENV_NAME_PATTERN;
import static org.apache.commons.lang3.Validate.matchesPattern;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ConfigEnv(String name) {

    public ConfigEnv {
        matchesPattern(name, APP_AND_ENV_NAME_PATTERN, "invalid environment name: " + APP_AND_ENV_NAME_MESSAGE);
    }
}
