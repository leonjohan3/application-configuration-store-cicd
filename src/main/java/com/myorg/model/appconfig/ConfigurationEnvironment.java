package com.myorg.model.appconfig;

import jakarta.validation.constraints.NotBlank;

public record ConfigurationEnvironment(@NotBlank String id, @NotBlank String name, @NotBlank String state) {

}
