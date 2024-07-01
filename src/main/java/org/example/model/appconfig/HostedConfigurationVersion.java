package org.example.model.appconfig;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HostedConfigurationVersion(@NotNull @Min(1) int versionNumber) {

}
