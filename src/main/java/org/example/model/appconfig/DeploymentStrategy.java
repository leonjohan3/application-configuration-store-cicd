package org.example.model.appconfig;

import jakarta.validation.constraints.NotBlank;

public record DeploymentStrategy(@NotBlank String strategyId, @NotBlank String name) {

}
