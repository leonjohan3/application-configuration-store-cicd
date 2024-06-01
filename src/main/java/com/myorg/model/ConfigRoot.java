package com.myorg.model;

import java.util.Set;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ConfigRoot(String rootFolder, Set<ConfigApp> applications) {

}
