package com.myorg.model.appconfig;

import jakarta.validation.constraints.NotBlank;

public record ConfigurationProfile(@NotBlank String id, @NotBlank String name) {

}
