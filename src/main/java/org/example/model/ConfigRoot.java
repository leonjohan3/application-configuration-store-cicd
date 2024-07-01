package org.example.model;

import static java.util.Set.copyOf;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Set;
import org.apache.commons.lang3.builder.Builder;
import org.example.model.ConfigRoot.ConfigRootBuilder;

@JsonDeserialize(builder = ConfigRootBuilder.class)
public record ConfigRoot(String rootFolder, Set<ConfigApp> applications) { // TODO - make rootFolder type Path

    public ConfigRoot(final String rootFolder, final Set<ConfigApp> applications) {
        notBlank(rootFolder, "rootFolder must not be null or blank");
        notNull(applications, "applications must not be null");
        this.rootFolder = rootFolder;
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

        private String rootFolder;
        private Set<ConfigApp> applications;

        public ConfigRootBuilder rootFolder(final String rootFolder) {
            this.rootFolder = rootFolder;
            return this;
        }

        public ConfigRootBuilder applications(final Set<ConfigApp> applications) {
            this.applications = copyOf(applications);
            return this;
        }

        @Override
        public ConfigRoot build() {
            return new ConfigRoot(rootFolder, applications);
        }
    }
}
