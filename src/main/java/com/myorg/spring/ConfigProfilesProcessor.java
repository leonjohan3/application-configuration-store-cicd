package com.myorg.spring;

import static com.myorg.constants.ServiceConstants.APP_PREFIX_MESSAGE;
import static com.myorg.constants.ServiceConstants.APP_PREFIX_PATTERN;
import static com.myorg.constants.ServiceConstants.MAX_WALK_DEPTH;
import static org.apache.commons.lang3.StringUtils.removeStart;

import com.myorg.model.ConfigApp;
import com.myorg.model.ConfigEnv;
import com.myorg.model.appconfig.Application;
import com.myorg.model.appconfig.ConfigurationProfile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
@Validated
public class ConfigProfilesProcessor {

    private final AppConfigFacade appConfigFacade;

    public @NotNull Set<ConfigApp> run(@NotNull final Path rootConfigFolder,
        @NotNull @Pattern(regexp = APP_PREFIX_PATTERN, message = APP_PREFIX_MESSAGE) final String applicationPrefix) {

        final var configApps = new HashSet<ConfigApp>();

        appConfigFacade.listApplications(applicationPrefix)
            .forEach(application -> processApplication(application, rootConfigFolder, applicationPrefix).ifPresent(configApps::add));

        return configApps;
    }

    private Optional<ConfigApp> processApplication(final Application application, final Path rootConfigFolder, final String applicationPrefix) {
        final var configEnvs = new HashSet<ConfigEnv>();

        appConfigFacade.listConfigurationProfiles(application)
            .forEach(configurationProfile -> processConfigurationProfile(application, configurationProfile, Path.of(rootConfigFolder.toString(),
                removeStart(application.name(), applicationPrefix + "/"))).ifPresent(configEnvs::add));

        return Optional.ofNullable(configEnvs.isEmpty() ? null : new ConfigApp(application.name(), configEnvs));
    }

    private Optional<ConfigEnv> processConfigurationProfile(final Application application, final ConfigurationProfile configurationProfile, final Path path) {

        final var environmentPath = Path.of(path.toString(), configurationProfile.name());
        final var latestVersion = getLatestHostedConfigVersion(application, configurationProfile);
        Optional<Path> configFileToUseForUpdate = Optional.empty();

        if (latestVersion.isPresent()) {
            configFileToUseForUpdate = checkIfAnUpdateIsRequired(application, configurationProfile, latestVersion.get(), environmentPath);
        }
        return configFileToUseForUpdate.map(configFilePath -> new ConfigEnv(configurationProfile.name(), configFilePath.toString()));
    }

    private Optional<Integer> getLatestHostedConfigVersion(final Application application, final ConfigurationProfile configurationProfile) {
        final var versions = new TreeSet<Integer>();

        appConfigFacade.listHostedConfigurationVersions(application, configurationProfile).forEach(hostedConfigurationVersion -> versions.add(
            hostedConfigurationVersion.versionNumber()));

        return Optional.ofNullable(versions.isEmpty() ? null : versions.last());
    }

    private Optional<Path> checkIfAnUpdateIsRequired(final Application application, final ConfigurationProfile configurationProfile, final int version,
        final Path path) {

        var hasDiff = false;
        final var configFileAndContent = getConfigFileAndContent(path);

        if (configFileAndContent.isPresent()) {
            final var hostedConfigVersionContent = appConfigFacade.getHostedConfigVersionContent(application, configurationProfile, version);
            hasDiff = !configFileAndContent.get().getRight().equals(hostedConfigVersionContent);
        }

        return Optional.ofNullable(hasDiff ? configFileAndContent.get().getLeft() : null);
    }

    private Optional<Pair<Path, String>> getConfigFileAndContent(final Path path) {

        try (var environmentPaths = Files.walk(path, MAX_WALK_DEPTH)) {

            final var configFile = environmentPaths.filter(environmentPath -> environmentPath.toFile().isFile()).findFirst();
            return Optional.ofNullable(configFile.isPresent() ? new ImmutablePair<>(configFile.get(), Files.readString(configFile.get())) : null);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
