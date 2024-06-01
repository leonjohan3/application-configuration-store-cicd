package com.myorg.model;

import java.util.Objects;
import java.util.Set;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ConfigApp(String name, Set<ConfigEnv> environments) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigApp configApp = (ConfigApp) o;
        return Objects.equals(name, configApp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
