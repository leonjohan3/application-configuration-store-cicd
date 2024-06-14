package com.myorg.model.appconfig;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HostedConfigurationVersion(@NotNull @Min(1) Integer versionNumber) {

}
