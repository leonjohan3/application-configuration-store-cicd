package org.example.model.appconfig;

import jakarta.validation.constraints.NotBlank;

public record DeploymentStrategy(@NotBlank String id, @NotBlank String name) {

}
