package org.example.model.appconfig;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record Deployment(@NotNull @Min(1) int deploymentNumber, @NotNull @Min(1) int versionNumber) {

}
