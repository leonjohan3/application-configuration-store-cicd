package org.example.spring;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.example.Utilities.extractPlainApplicationName;
import static org.example.constants.ServiceConstants.CONFIG_GRP_MESSAGE;
import static org.example.constants.ServiceConstants.CONFIG_GRP_PATTERN;
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
@SuppressFBWarnings("EI_EXPOSE_REP2")
@SuppressWarnings({"PMD.GuardLogStatement"})
public class AppConfigFacade {

    private final AppConfigClient appConfigClient;

    public @NotNull List<@Valid Application> listApplications(
        @NotNull @Pattern(regexp = CONFIG_GRP_PATTERN, message = CONFIG_GRP_MESSAGE) final String configGroupPrefix) {

        final var applications = new ArrayList<Application>();
        String nextToken = null;

        do {
            final ListApplicationsResponse listApplicationsResponse;

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
            final ListConfigurationProfilesResponse profilesResponse;

            if (isNull(nextToken)) {
                profilesResponse = appConfigClient.listConfigurationProfiles(builder -> builder.applicationId(application.applicationId()));
            } else {
                final var finalNextToken = nextToken;
                profilesResponse = appConfigClient.listConfigurationProfiles(
                    builder -> builder.applicationId(application.applicationId()).nextToken(finalNextToken));
            }
            nextToken = profilesResponse.nextToken();
            configurationProfiles.addAll(profilesResponse.items().stream().map(item -> new ConfigurationProfile(item.id(), item.name())).toList());

        } while (nonNull(nextToken));

        return configurationProfiles;
    }

    public @NotEmpty List<@Valid ConfigurationEnvironment> listConfigurationEnvironments(@Valid final Application application) {

        final var configEnvironments = new ArrayList<ConfigurationEnvironment>();
        String nextToken = null;

        do {
            final ListEnvironmentsResponse listEnvironmentsResponse;

            if (isNull(nextToken)) {
                listEnvironmentsResponse = appConfigClient.listEnvironments(builder -> builder.applicationId(application.applicationId()));
            } else {
                final var finalNextToken = nextToken;
                listEnvironmentsResponse = appConfigClient.listEnvironments(
                    builder -> builder.applicationId(application.applicationId()).nextToken(finalNextToken));
            }
            nextToken = listEnvironmentsResponse.nextToken();
            configEnvironments.addAll(
                listEnvironmentsResponse.items().stream().map(item -> new ConfigurationEnvironment(item.id(), item.name(), item.stateAsString())).toList());

        } while (nonNull(nextToken));

        return configEnvironments;
    }

    public @NotNull List<@Valid HostedConfigurationVersion> listHostedConfigurationVersions(@Valid final Application application,
        @Valid final ConfigurationProfile configurationProfile) {

        String nextToken = null;
        final var hostedConfigVersions = new ArrayList<HostedConfigurationVersion>();

        do {
            final ListHostedConfigurationVersionsResponse versionsResponse;

            if (isNull(nextToken)) {
                versionsResponse = appConfigClient.listHostedConfigurationVersions(
                    builder -> builder.applicationId(application.applicationId()).configurationProfileId(configurationProfile.profileId()));
            } else {
                final var finalNextToken = nextToken;
                versionsResponse = appConfigClient.listHostedConfigurationVersions(
                    builder -> builder.applicationId(application.applicationId()).configurationProfileId(configurationProfile.profileId())
                        .nextToken(finalNextToken));
            }
            nextToken = versionsResponse.nextToken();
            hostedConfigVersions.addAll(versionsResponse.items().stream().map(item -> new HostedConfigurationVersion(item.versionNumber())).toList());

        } while (nonNull(nextToken));

        return hostedConfigVersions;
    }

    public @NotNull List<@Valid Deployment> listDeployments(@Valid final Application application, @Valid final ConfigurationEnvironment configurationEnvironment) {

        String nextToken = null;
        final var deployments = new ArrayList<Deployment>();

        do {
            final ListDeploymentsResponse listDeploymentsResponse;

            if (isNull(nextToken)) {
                listDeploymentsResponse = appConfigClient.listDeployments(
                    builder -> builder.applicationId(application.applicationId()).environmentId(configurationEnvironment.environmentId()));
            } else {
                final var finalNextToken = nextToken;
                listDeploymentsResponse = appConfigClient.listDeployments(
                    builder -> builder.applicationId(application.applicationId()).environmentId(configurationEnvironment.environmentId())
                        .nextToken(finalNextToken));
            }
            nextToken = listDeploymentsResponse.nextToken();
            deployments.addAll(
                listDeploymentsResponse.items().stream().map(item -> new Deployment(item.deploymentNumber(), parseInt(item.configurationVersion()))).toList());

        } while (nonNull(nextToken));

        return deployments;
    }

    public @NotEmpty List<@Valid DeploymentStrategy> listDeploymentStrategies() {

        String nextToken = null;
        final var deploymentStrategies = new ArrayList<DeploymentStrategy>();

        do {
            final ListDeploymentStrategiesResponse strategiesResponse;

            if (isNull(nextToken)) {
                strategiesResponse = appConfigClient.listDeploymentStrategies(builder -> {
                });
            } else {
                final var finalNextToken = nextToken;
                strategiesResponse = appConfigClient.listDeploymentStrategies(builder -> builder.nextToken(finalNextToken));
            }
            nextToken = strategiesResponse.nextToken();
            deploymentStrategies.addAll(strategiesResponse.items().stream().map(item -> new DeploymentStrategy(item.id(), item.name())).toList());

        } while (nonNull(nextToken));

        return deploymentStrategies;
    }

    public void createHostedConfigVersion(@Valid final Application application, @Valid final ConfigurationProfile configurationProfile,
        @NotNull final String content) {

        appConfigClient.createHostedConfigurationVersion(
            builder -> builder.applicationId(application.applicationId())
                .configurationProfileId(configurationProfile.profileId())
                .content(fromUtf8String(content))
                .contentType(TEXT_PLAIN_VALUE)
                .description(format("Configuration that will be deployed to the `%s` environment of the `%s` application, created %s", configurationProfile.name(),
                    application.plainName(), ISO_ZONED_DATE_TIME.format(ZonedDateTime.now())))
                .build());
    }

    public void deleteHostedConfigVersion(@Valid final Application application, @Valid final ConfigurationProfile configurationProfile,
        @Min(1) final int hostedConfigVersion) {

        appConfigClient.deleteHostedConfigurationVersion(builder -> builder.applicationId(application.applicationId())
            .configurationProfileId(configurationProfile.profileId())
            .versionNumber(hostedConfigVersion)
            .build());
    }

    public void deployHostedConfigVersion(@Valid final Application application, @Valid final ConfigurationProfile configurationProfile,
        @Valid final ConfigurationEnvironment configurationEnvironment, @Min(1) final int hostedConfigVersion, @NotBlank final String deploymentStrategyId) {

        appConfigClient.startDeployment(builder -> builder.applicationId(application.applicationId()).configurationProfileId(configurationProfile.profileId())
            .configurationVersion(valueOf(hostedConfigVersion)).environmentId(configurationEnvironment.environmentId()).deploymentStrategyId(deploymentStrategyId)
            .build());
    }

    public @NotNull String getHostedConfigVersionContent(@Valid final Application application, @Valid final ConfigurationProfile configurationProfile,
        @Min(1) final int configVersion) {

        String configVersionContent = null;

        try {
            final var configVersionResponse = appConfigClient.getHostedConfigurationVersion(
                    builder -> builder.applicationId(application.applicationId()).configurationProfileId(configurationProfile.profileId())
                        .versionNumber(configVersion)).content();

            configVersionContent = configVersionResponse.asUtf8String();

        } catch (ResourceNotFoundException rnfe) {
            log.error("unable to retrieve configVersionContent: application:{}, configurationProfile:{}, configVersion:{}", application.applicationId(),
                configurationProfile.profileId(), configVersion, rnfe);
        }
        return requireNonNull(configVersionContent, "configVersionContent should not be be null");
    }
}
