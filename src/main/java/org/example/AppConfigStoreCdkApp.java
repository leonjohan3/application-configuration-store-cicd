package org.example;

import static java.lang.System.getenv;
import static org.apache.commons.lang3.Validate.matchesPattern;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.example.constants.ServiceConstants.CONFIG_GRP_MESSAGE;
import static org.example.constants.ServiceConstants.CONFIG_GRP_PATTERN;
import static org.example.constants.ServiceConstants.GROUP_PREFIX;
import static org.example.constants.ServiceConstants.GROUP_PREFIX_ENV;
import static org.example.constants.ServiceConstants.ROOT_CONFIG_FOLDER_ENV;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import lombok.experimental.UtilityClass;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

@UtilityClass
public class AppConfigStoreCdkApp {

    public static void main(final String[] args) throws IOException {
        final var rootFolderEnvVar = getenv(ROOT_CONFIG_FOLDER_ENV);
        notBlank(rootFolderEnvVar, "env var " + ROOT_CONFIG_FOLDER_ENV + " should be set");
        final var configGroupEnvVar = getenv(GROUP_PREFIX_ENV);
        notBlank(configGroupEnvVar, "env var " + GROUP_PREFIX_ENV + " should be set");
        matchesPattern(configGroupEnvVar, CONFIG_GRP_PATTERN, CONFIG_GRP_MESSAGE);
        final var app = new App();

        new AppConfigStoreCdkStack(app, "applicationConfigurationStore-" + configGroupEnvVar, StackProps.builder()
            .description("AWS AppConfig CI/CD for the application configuration store: " + configGroupEnvVar)
            .tags(Map.of(GROUP_PREFIX, configGroupEnvVar))
            .terminationProtection(true)
            .build(), Path.of(rootFolderEnvVar), configGroupEnvVar);

        app.synth();
    }
}

