package com.myorg.spring;

import static com.myorg.constants.ServiceConstants.CONFIG_GROUP_PREFIX_MESSAGE;
import static com.myorg.constants.ServiceConstants.CONFIG_GROUP_PREFIX_PATTERN;
import static java.lang.String.format;

import com.myorg.model.ConfigApp;
import com.myorg.model.ConfigEnv;
import com.myorg.model.appconfig.Application;
import com.myorg.model.appconfig.ConfigurationEnvironment;
import com.myorg.model.appconfig.ConfigurationProfile;
import com.myorg.model.appconfig.Deployment;
import com.myorg.model.appconfig.DeploymentStrategy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
@Validated
public class ConfigProfileDeployer {

    private final AppConfigFacade appConfigFacade;
    private final ConfigVersionService configVersionService;

    public @NotNull Set<ConfigApp> run(
        @NotNull @Pattern(regexp = CONFIG_GROUP_PREFIX_PATTERN, message = CONFIG_GROUP_PREFIX_MESSAGE) final String configGroupPrefix) {

        final var configApps = new HashSet<ConfigApp>();
        final var deploymentStrategy = getDeploymentStrategy(configGroupPrefix);

        if (deploymentStrategy.isEmpty()) {
            throw new IllegalStateException(format("unable to get deployment strategy for configGroupPrefix `%s`", configGroupPrefix));
        }
        appConfigFacade.listApplications(configGroupPrefix)
            .forEach(application -> processApplication(application, deploymentStrategy.get()).ifPresent(configApps::add));
        return configApps;
    }

    private Optional<ConfigApp> processApplication(final Application application, final DeploymentStrategy deploymentStrategy) {
        final var configEnvs = new HashSet<ConfigEnv>();

        appConfigFacade.listConfigurationProfiles(application)
            .forEach(configurationProfile -> processConfigurationProfile(application, configurationProfile, deploymentStrategy).ifPresent(configEnvs::add));

        return Optional.ofNullable(configEnvs.isEmpty() ? null : new ConfigApp(application.plainName(), configEnvs));
    }

    private Optional<ConfigEnv> processConfigurationProfile(final Application application, final ConfigurationProfile configurationProfile,
        final DeploymentStrategy deploymentStrategy) {

        Optional<ConfigEnv> configEnv = Optional.empty();
        final var optionalConfigurationEnvironment = getConfigurationEnvironment(application, configurationProfile);

        if (optionalConfigurationEnvironment.isEmpty()) {
            throw new IllegalStateException(
                format("unable to get configuration environment for application `%s`, and environment `%s`", application.name(), configurationProfile.name()));
        }
        final var configurationEnvironment = optionalConfigurationEnvironment.get();

        if (!configurationEnvironment.state().equals("ReadyForDeployment")) {
            throw new IllegalStateException(
                format("configuration environment not ready for deployment for application `%s`, and environment `%s`", application.name(),
                    configurationProfile.name()));
        }

        final var hostedConfigVersion = configVersionService.getLatestHostedConfigVersion(application, configurationProfile);

        if (hostedConfigVersion.isPresent()) {
            final var latestDeployedVersion = getLatestDeploymentVersion(application, configurationEnvironment);

            if (hostedConfigVersion.get() > latestDeployedVersion) {
                appConfigFacade.deployHostedConfigVersion(application, configurationProfile, configurationEnvironment, hostedConfigVersion.get(),
                    deploymentStrategy.id());
                configEnv = Optional.of(new ConfigEnv(configurationProfile.name()));
            }
        }
        return configEnv;
    }

    private Optional<DeploymentStrategy> getDeploymentStrategy(final String configGroupPrefix) {
        return appConfigFacade.listDeploymentStrategies().stream()
            .filter(deploymentStrategy -> deploymentStrategy.name().equals(configGroupPrefix + "-all-at-once-with-no-bake-time-cdk")).findFirst();
    }

    private Optional<ConfigurationEnvironment> getConfigurationEnvironment(final Application application, final ConfigurationProfile configurationProfile) {
        return appConfigFacade.listConfigurationEnvironments(application).stream()
            .filter(configurationEnvironment -> configurationEnvironment.name().equals(configurationProfile.name())).findFirst();
    }

    private int getLatestDeploymentVersion(final Application application, final ConfigurationEnvironment configurationEnvironment) {
        final var deployments = new TreeMap<Integer, Deployment>();
        appConfigFacade.listDeployments(application, configurationEnvironment).forEach(deployment -> deployments.put(deployment.deploymentNumber(), deployment));
        return deployments.isEmpty() ? 0 : deployments.lastEntry().getValue().versionNumber();
    }
}
