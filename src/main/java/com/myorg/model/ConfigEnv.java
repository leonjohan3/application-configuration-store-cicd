package com.myorg.model;

import java.util.Objects;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ConfigEnv(String name, String configFilePath) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigEnv configEnv = (ConfigEnv) o;
        return Objects.equals(name, configEnv.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
