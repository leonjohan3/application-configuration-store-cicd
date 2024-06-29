package com.myorg.model.appconfig;

import jakarta.validation.constraints.NotBlank;

public record Application(@NotBlank String id, @NotBlank String name, @NotBlank String plainName) {

}
