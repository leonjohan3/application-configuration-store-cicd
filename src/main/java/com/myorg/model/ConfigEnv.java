package com.myorg.model;

import static com.myorg.constants.ServiceConstants.APP_AND_ENV_NAME_MESSAGE;
import static com.myorg.constants.ServiceConstants.APP_AND_ENV_NAME_PATTERN;
import static org.apache.commons.lang3.Validate.matchesPattern;
import static org.apache.commons.lang3.Validate.notBlank;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.HashCodeExclude;

@Builder
@Jacksonized
public record ConfigEnv(String name, @EqualsExclude @HashCodeExclude String configFilePath/*TODO: consider making this type Path*/) {

    public ConfigEnv {
        matchesPattern(name, APP_AND_ENV_NAME_PATTERN, "invalid environment name: " + APP_AND_ENV_NAME_MESSAGE);
        notBlank(configFilePath, "configFilePath must not be null or blank");
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
