package com.myorg.spring;

import static com.myorg.constants.ServiceConstants.CONFIG_GROUP_PREFIX_MESSAGE;
import static com.myorg.constants.ServiceConstants.CONFIG_GROUP_PREFIX_PATTERN;
import static com.myorg.constants.ServiceConstants.MAX_WALK_DEPTH;
import static java.nio.file.Files.readString;

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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
@Validated
@Slf4j
public class ConfigProfilesProcessor {

    private final AppConfigFacade appConfigFacade;

    public @NotNull Set<ConfigApp> run(@NotNull final Path rootConfigFolder,
        @NotNull @Pattern(regexp = CONFIG_GROUP_PREFIX_PATTERN, message = CONFIG_GROUP_PREFIX_MESSAGE) final String configGroupPrefix, final boolean update) {

        final var configApps = new HashSet<ConfigApp>();

        appConfigFacade.listApplications(configGroupPrefix)
            .forEach(application -> processApplication(application, rootConfigFolder, update).ifPresent(configApps::add));

        return configApps;
    }

    private Optional<ConfigApp> processApplication(final Application application, final Path rootConfigFolder, final boolean update) {

        final var configEnvs = new HashSet<ConfigEnv>();

        appConfigFacade.listConfigurationProfiles(application)
            .forEach(configurationProfile -> processConfigurationProfile(application, configurationProfile,
                Path.of(rootConfigFolder.toString(), application.plainName()), update).ifPresent(configEnvs::add));

        return Optional.ofNullable(configEnvs.isEmpty() ? null : new ConfigApp(application.plainName(), configEnvs));
    }

    private Optional<ConfigEnv> processConfigurationProfile(final Application application, final ConfigurationProfile configurationProfile, final Path path,
        final boolean update) {

        final var environmentPath = Path.of(path.toString(), configurationProfile.name());
        final var latestVersion = getLatestHostedConfigVersion(application, configurationProfile);
        Optional<Path> configFileToUseForUpdate = Optional.empty();

        if (latestVersion.isPresent()) {
            final var checkIfAnUpdateIsRequiredResult = checkIfAnUpdateIsRequired(application, configurationProfile, latestVersion.get(), environmentPath);

            if (checkIfAnUpdateIsRequiredResult.isPresent()) {

                if (update) { // update HostedConfigVersion (by creating a new version)
                    appConfigFacade.createHostedConfigVersion(application, configurationProfile, checkIfAnUpdateIsRequiredResult.get().getRight());
                }
                configFileToUseForUpdate = Optional.of(checkIfAnUpdateIsRequiredResult.get().getLeft());
            }

        } else {
            if (update) { // create 1st HostedConfigVersion (version 1)
                final var configFileAndContent = getConfigFileAndContent(environmentPath);

                if (configFileAndContent.isPresent()) {
                    appConfigFacade.createHostedConfigVersion(application, configurationProfile, configFileAndContent.get().getRight());
                    configFileToUseForUpdate = Optional.of(configFileAndContent.get().getLeft());
                }
            }
        }
        return configFileToUseForUpdate.map(configFilePath -> new ConfigEnv(configurationProfile.name(), configFilePath.toString()));
    }

    private Optional<Integer> getLatestHostedConfigVersion(final Application application, final ConfigurationProfile configurationProfile) {
        final var versions = new TreeSet<Integer>();

        appConfigFacade.listHostedConfigurationVersions(application, configurationProfile).forEach(hostedConfigurationVersion -> versions.add(
            hostedConfigurationVersion.versionNumber()));

        return Optional.ofNullable(versions.isEmpty() ? null : versions.last());
    }

    private Optional<Pair<Path, String>> checkIfAnUpdateIsRequired(final Application application, final ConfigurationProfile configurationProfile,
        final int version, final Path path) {

        var hasDiff = false;
        final var configFileAndContent = getConfigFileAndContent(path);

        if (configFileAndContent.isPresent()) {
            final var hostedConfigVersionContent = appConfigFacade.getHostedConfigVersionContent(application, configurationProfile, version);
            hasDiff = !configFileAndContent.get().getRight().equals(hostedConfigVersionContent);
        }

        return Optional.ofNullable(hasDiff ? configFileAndContent.get() : null);
    }

    private Optional<Pair<Path, String>> getConfigFileAndContent(final Path path) {

        try (var environmentPaths = Files.walk(path, MAX_WALK_DEPTH)) {

            final var configFile = environmentPaths.filter(environmentPath -> environmentPath.toFile().isFile()).findFirst();
            return Optional.ofNullable(configFile.isPresent() ? new ImmutablePair<>(configFile.get(), readString(configFile.get())) : null);

        } catch (NoSuchFileException e) {
            log.debug("ignoring NoSuchFileException");
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
