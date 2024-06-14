package com.myorg.model;

import static java.util.Set.copyOf;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.myorg.model.ConfigRoot.ConfigRootBuilder;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import org.apache.commons.lang3.builder.Builder;

@JsonDeserialize(builder = ConfigRootBuilder.class)
public record ConfigRoot(@NotBlank String rootFolder, Set<ConfigApp> applications) {

    public ConfigRoot(final String rootFolder, final Set<ConfigApp> applications) {
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
