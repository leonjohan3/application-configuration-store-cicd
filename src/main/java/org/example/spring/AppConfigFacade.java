package org.example.spring;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.example.Util.extractPlainApplicationName;
import static org.example.constants.ServiceConstants.CONFIG_GROUP_PREFIX_MESSAGE;
import static org.example.constants.ServiceConstants.CONFIG_GROUP_PREFIX_PATTERN;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;
import static software.amazon.awssdk.core.SdkBytes.fromUtf8String;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.appconfig.Application;
import org.example.model.appconfig.ConfigurationEnvironment;
import org.example.model.appconfig.ConfigurationProfile;
import org.example.model.appconfig.Deployment;
import org.example.model.appconfig.DeploymentStrategy;
import org.example.model.appconfig.HostedConfigurationVersion;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfig.model.ListApplicationsResponse;
import software.amazon.awssdk.services.appconfig.model.ListConfigurationProfilesResponse;
import software.amazon.awssdk.services.appconfig.model.ListDeploymentStrategiesResponse;
import software.amazon.awssdk.services.appconfig.model.ListDeploymentsResponse;
import software.amazon.awssdk.services.appconfig.model.ListEnvironmentsResponse;
import software.amazon.awssdk.services.appconfig.model.ListHostedConfigurationVersionsResponse;
import software.amazon.awssdk.services.appconfig.model.ResourceNotFoundException;

@Component
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
@Validated
@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
public class AppConfigFacade {

    private final AppConfigClient appConfigClient;

    public @NotNull List<@Valid Application> listApplications(
        @NotNull @Pattern(regexp = CONFIG_GROUP_PREFIX_PATTERN, message = CONFIG_GROUP_PREFIX_MESSAGE) final String configGroupPrefix) {

        final var applications = new ArrayList<Application>();
        String nextToken = null;

        do {
            ListApplicationsResponse listApplicationsResponse;

            if (isNull(nextToken)) {
                listApplicationsResponse = appConfigClient.listApplications(listApplicationsRequest -> {
                });
            } else {
                final var finalNextToken = nextToken;
                listApplicationsResponse = appConfigClient.listApplications(builder -> builder.nextToken(finalNextToken));
            }
            nextToken = listApplicationsResponse.nextToken();

            applications.addAll(listApplicationsResponse.items().stream().filter(item -> item.name().startsWith(configGroupPrefix + "/"))
                .map(item -> new Application(item.id(), item.name(), extractPlainApplicationName(item.name(), configGroupPrefix))).toList());

        } while (nonNull(nextToken));

        return applications;
    }

    public @NotEmpty List<@Valid ConfigurationProfile> listConfigurationProfiles(@Valid final Application application) {

        final var configurationProfiles = new ArrayList<ConfigurationProfile>();
        String nextToken = null;

        do {
            ListConfigurationProfilesResponse listConfigProfilesResponse;

            if (Objects.isNull(nextToken)) {
                listConfigProfilesResponse = appConfigClient.listConfigurationProfiles(builder -> builder.applicationId(application.id()));
            } else {
                final var finalNextToken = nextToken;
                listConfigProfilesResponse = appConfigClient.listConfigurationProfiles(
                    builder -> builder.applicationId(application.id()).nextToken(finalNextToken));
            }
            nextToken = listConfigProfilesResponse.nextToken();
            configurationProfiles.addAll(listConfigProfilesResponse.items().stream().map(item -> new ConfigurationProfile(item.id(), item.name())).toList());

        } while (Objects.nonNull(nextToken));

        return configurationProfiles;
    }

    public @NotEmpty List<@Valid ConfigurationEnvironment> listConfigurationEnvironments(@Valid final Application application) {

        final var configurationEnvironments = new ArrayList<ConfigurationEnvironment>();
        String nextToken = null;

        do {
            ListEnvironmentsResponse listEnvironmentsResponse;

            if (Objects.isNull(nextToken)) {
                listEnvironmentsResponse = appConfigClient.listEnvironments(builder -> builder.applicationId(application.id()));
            } else {
                final var finalNextToken = nextToken;
                listEnvironmentsResponse = appConfigClient.listEnvironments(builder -> builder.applicationId(application.id()).nextToken(finalNextToken));
            }
            nextToken = listEnvironmentsResponse.nextToken();
            configurationEnvironments.addAll(
                listEnvironmentsResponse.items().stream().map(item -> new ConfigurationEnvironment(item.id(), item.name(), item.stateAsString())).toList());

        } while (Objects.nonNull(nextToken));

        return configurationEnvironments;
    }

    public @NotNull List<@Valid HostedConfigurationVersion> listHostedConfigurationVersions(@Valid final Application application,
        @Valid final ConfigurationProfile configurationProfile) {

        String nextToken = null;
        final var hostedConfigVersions = new ArrayList<HostedConfigurationVersion>();

        do {
            ListHostedConfigurationVersionsResponse listHostedConfigVersionsResponse;

            if (Objects.isNull(nextToken)) {
                listHostedConfigVersionsResponse = appConfigClient.listHostedConfigurationVersions(
                    builder -> builder.applicationId(application.id()).configurationProfileId(configurationProfile.id()));
            } else {
                final var finalNextToken = nextToken;
                listHostedConfigVersionsResponse = appConfigClient.listHostedConfigurationVersions(
                    builder -> builder.applicationId(application.id()).configurationProfileId(configurationProfile.id()).nextToken(finalNextToken));
            }
            nextToken = listHostedConfigVersionsResponse.nextToken();
            hostedConfigVersions.addAll(
                listHostedConfigVersionsResponse.items().stream().map(item -> new HostedConfigurationVersion(item.versionNumber())).toList());

        } while (Objects.nonNull(nextToken));

        return hostedConfigVersions;
    }

    public @NotNull List<@Valid Deployment> listDeployments(@Valid final Application application, @Valid final ConfigurationEnvironment configurationEnvironment) {

        String nextToken = null;
        final var deployments = new ArrayList<Deployment>();

        do {
            ListDeploymentsResponse listDeploymentsResponse;

            if (Objects.isNull(nextToken)) {
                listDeploymentsResponse = appConfigClient.listDeployments(
                    builder -> builder.applicationId(application.id()).environmentId(configurationEnvironment.id()));
            } else {
                final var finalNextToken = nextToken;
                listDeploymentsResponse = appConfigClient.listDeployments(
                    builder -> builder.applicationId(application.id()).environmentId(configurationEnvironment.id()).nextToken(finalNextToken));
            }
            nextToken = listDeploymentsResponse.nextToken();
            deployments.addAll(
                listDeploymentsResponse.items().stream().map(item -> new Deployment(item.deploymentNumber(), parseInt(item.configurationVersion()))).toList());

        } while (Objects.nonNull(nextToken));

        return deployments;
    }

    public @NotEmpty List<@Valid DeploymentStrategy> listDeploymentStrategies() {

        String nextToken = null;
        final var deploymentStrategies = new ArrayList<DeploymentStrategy>();

        do {
            ListDeploymentStrategiesResponse listDeploymentStrategiesResponse;

            if (Objects.isNull(nextToken)) {
                listDeploymentStrategiesResponse = appConfigClient.listDeploymentStrategies(builder -> {
                });
            } else {
                final var finalNextToken = nextToken;
                listDeploymentStrategiesResponse = appConfigClient.listDeploymentStrategies(builder -> builder.nextToken(finalNextToken));
            }
            nextToken = listDeploymentStrategiesResponse.nextToken();
            deploymentStrategies.addAll(
                listDeploymentStrategiesResponse.items().stream().map(item -> new DeploymentStrategy(item.id(), item.name())).toList());

        } while (Objects.nonNull(nextToken));

        return deploymentStrategies;
    }

    public void createHostedConfigVersion(@Valid final Application application, @Valid final ConfigurationProfile configurationProfile,
        @NotNull final String content) {

        appConfigClient.createHostedConfigurationVersion(
            builder -> builder.applicationId(application.id())
                .configurationProfileId(configurationProfile.id())
                .content(fromUtf8String(content))
                .contentType(TEXT_PLAIN_VALUE)
                .description(format("Configuration that will be deployed to the `%s` environment of the `%s` application, created %s", configurationProfile.name(),
                    application.plainName(), ISO_ZONED_DATE_TIME.format(ZonedDateTime.now())))
                .build());
    }

    public void deleteHostedConfigVersion(@Valid final Application application, @Valid final ConfigurationProfile configurationProfile,
        @Min(1) final int hostedConfigurationVersion) {

        appConfigClient.deleteHostedConfigurationVersion(builder -> builder.applicationId(application.id())
            .configurationProfileId(configurationProfile.id())
            .versionNumber(hostedConfigurationVersion)
            .build());
    }

    public void deployHostedConfigVersion(@Valid final Application application, @Valid final ConfigurationProfile configurationProfile,
        @Valid final ConfigurationEnvironment configurationEnvironment, @Min(1) final int hostedConfigurationVersion, @NotBlank final String deploymentStrategyId) {

        appConfigClient.startDeployment(builder -> builder.applicationId(application.id()).configurationProfileId(configurationProfile.id())
            .configurationVersion(valueOf(hostedConfigurationVersion)).environmentId(configurationEnvironment.id()).deploymentStrategyId(deploymentStrategyId)
            .build());
    }

    public @NotNull String getHostedConfigVersionContent(@Valid final Application application,
        @Valid final ConfigurationProfile configurationProfile, @Min(1) final int hostedConfigurationVersion) {

        String hostedConfigVersionContent = null;

        try {
            final var hostedConfigurationVersionResponse = appConfigClient.getHostedConfigurationVersion(
                hcvr -> hcvr.applicationId(application.id()).configurationProfileId(configurationProfile.id())
                    .versionNumber(hostedConfigurationVersion)).content();

            hostedConfigVersionContent = hostedConfigurationVersionResponse.asUtf8String();

        } catch (ResourceNotFoundException rnfe) {
            log.error("unable to retrieve hostedConfigVersionContent: application:{}, configurationProfile:{}, hostedConfigurationVersion:{}", application.id(),
                configurationProfile.id(), hostedConfigurationVersion, rnfe);
        }
        return Objects.requireNonNull(hostedConfigVersionContent, "hostedConfigVersionContent should not be be null");
    }
}
