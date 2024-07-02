package org.example.model;

import static java.util.Set.copyOf;
import static org.apache.commons.lang3.Validate.notNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Set;
import org.apache.commons.lang3.builder.Builder;
import org.example.model.ConfigRoot.ConfigRootBuilder;

@JsonDeserialize(builder = ConfigRootBuilder.class)
public record ConfigRoot(Set<ConfigApp> applications) {

    public ConfigRoot(final Set<ConfigApp> applications) {
        notNull(applications, "applications must not be null");
        this.applications = copyOf(applications);
    }

    public Set<ConfigApp> applications() {
        return copyOf(applications);
    }

    public static ConfigRootBuilder builder() {
        return new ConfigRootBuilder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ConfigRootBuilder implements Builder<ConfigRoot> {

        private Set<ConfigApp> theApplications;

        public ConfigRootBuilder applications(final Set<ConfigApp> applications) {
            this.theApplications = copyOf(applications);
            return this;
        }

        @Override
        public ConfigRoot build() {
            return new ConfigRoot(theApplications);
        }
    }
}
