package com.myorg;

import static org.apache.commons.lang3.StringUtils.capitalize;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.appconfig.Application;
import software.amazon.awscdk.services.appconfig.CfnConfigurationProfile;
import software.amazon.awscdk.services.appconfig.DeploymentStrategy;
import software.amazon.awscdk.services.appconfig.Environment;
import software.amazon.awscdk.services.appconfig.RolloutStrategy;
import software.amazon.awscdk.services.appconfig.RolloutStrategyProps;
import software.constructs.Construct;

public final class AppConfigStoreCdkStack extends Stack {

    public AppConfigStoreCdkStack(final Construct scope, final String id, final StackProps props, final Path rootFolder) throws IOException {
        super(scope, id, props);
        Objects.requireNonNull(rootFolder, "rootFolder must not be null");

        DeploymentStrategy.Builder.create(this, "applicationConfigurationStoreDeploymentStrategy")
            .rolloutStrategy(RolloutStrategy.linear(RolloutStrategyProps.builder()
                .deploymentDuration(Duration.minutes(0))
                .finalBakeTime(Duration.minutes(0))
                .growthFactor(100)
                .build()))
            .description("DeploymentStrategy for application configuration store that deploys without any delay")
            .deploymentStrategyName("acs-all-at-once-with-no-bake-time-cdk")
            .build();

        final var configRoot = ConfigRootFactory.createConfigRoot(rootFolder);

        configRoot.applications().forEach(configApp -> {

            final var application = Application.Builder.create(this, configApp.name() + "AppConfig")
                .applicationName("asc/" + configApp.name())
                .description(configApp.name())
                .build();

            configApp.environments().forEach(configEnv -> {
                Environment.Builder.create(this, configApp.name() + capitalize(configEnv.name()) + "ConfigEnv")
                    .application(application)
                    .environmentName(configEnv.name())
                    .description(String.format("`%s` environment of the `%s` application", configEnv.name(), configApp.name()))
                    .build();

                CfnConfigurationProfile.Builder.create(this, configApp.name() + capitalize(configEnv.name()) + "ConfigProfile")
                    .applicationId(application.getApplicationId())
                    .locationUri("hosted")
                    .name(configEnv.name())
                    .type("AWS.Freeform")
                    .build();
            });
        });
    }
}
