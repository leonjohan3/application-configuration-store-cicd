package org.example.model.appconfig;

import jakarta.validation.constraints.NotBlank;

public record ConfigurationProfile(@NotBlank String profileId, @NotBlank String name) {

}
