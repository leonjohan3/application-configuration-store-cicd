package com.myorg.model;

import static com.myorg.constants.ServiceConstants.APP_AND_ENV_NAME_MESSAGE;
import static com.myorg.constants.ServiceConstants.APP_AND_ENV_NAME_PATTERN;
import static java.util.Set.copyOf;
import static org.apache.commons.lang3.Validate.matchesPattern;
import static org.apache.commons.lang3.Validate.notEmpty;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Set;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.HashCodeExclude;

public record ConfigApp(String name, @EqualsExclude @HashCodeExclude Set<ConfigEnv> environments) {

    public ConfigApp(final String name, final Set<ConfigEnv> environments) {
        matchesPattern(name, APP_AND_ENV_NAME_PATTERN, "invalid application name: " + APP_AND_ENV_NAME_MESSAGE);
        notEmpty(environments, "environments must not be null or empty");
        this.name = name;
        this.environments = copyOf(environments);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public Set<ConfigEnv> environments() {
        return copyOf(environments);
    }

    public static ConfigAppBuilder builder() {
        return new ConfigAppBuilder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ConfigAppBuilder implements Builder<ConfigApp> {

        private String name;
        private Set<ConfigEnv> environments;

        public ConfigAppBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public ConfigAppBuilder environments(final Set<ConfigEnv> environments) {
            this.environments = copyOf(environments);
            return this;
        }

        @Override
        public ConfigApp build() {
            return new ConfigApp(name, environments);
        }
    }
}
