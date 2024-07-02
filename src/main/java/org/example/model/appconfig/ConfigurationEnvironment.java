package org.example.model.appconfig;

import jakarta.validation.constraints.NotBlank;

public record ConfigurationEnvironment(@NotBlank String environmentId, @NotBlank String name, @NotBlank String state) {

}
