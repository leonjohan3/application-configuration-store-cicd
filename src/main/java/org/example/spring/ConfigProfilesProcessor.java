package org.example.spring;

import static java.nio.file.Files.readString;
import static org.example.constants.ServiceConstants.CONFIG_GRP_MESSAGE;
import static org.example.constants.ServiceConstants.CONFIG_GRP_PATTERN;
import static org.example.constants.ServiceConstants.MAX_WALK_DEPTH;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.example.model.ConfigApp;
import org.example.model.ConfigEnv;
import org.example.model.appconfig.Application;
import org.example.model.appconfig.ConfigurationProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Validated
@Slf4j
public class ConfigProfilesProcessor {

    private final AppConfigFacade appConfigFacade;
    private final ConfigVersionService configVersionService;
    private final int configVersionsToKeep;

    public ConfigProfilesProcessor(final AppConfigFacade appConfigFacade, final ConfigVersionService configVersionService,
        @Value("${hosted.config.versions.to.keep:10}") final int configVersionsToKeep) {

        this.appConfigFacade = appConfigFacade;
        this.configVersionService = configVersionService;
        this.configVersionsToKeep = configVersionsToKeep;
    }

    public @NotNull Set<ConfigApp> run(@NotNull final Path rootConfigFolder,
        @NotNull @Pattern(regexp = CONFIG_GRP_PATTERN, message = CONFIG_GRP_MESSAGE) final String configGroupPrefix, final boolean update) {

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
        final var latestVersion = configVersionService.getLatestHostedConfigVersion(application, configurationProfile);
        Optional<Path> configFileToUseForUpdate = Optional.empty();

        if (latestVersion.isPresent()) {
            final var updateIsRequiredResult = checkIfAnUpdateIsRequired(application, configurationProfile, latestVersion.get(), environmentPath);

            if (updateIsRequiredResult.isPresent()) {

                if (update) { // update HostedConfigVersion (by creating a new version)
                    appConfigFacade.createHostedConfigVersion(application, configurationProfile, updateIsRequiredResult.get().getRight());
                    deleteOldAndUnusedHostedConfigVersions(application, configurationProfile);
                }
                configFileToUseForUpdate = Optional.of(updateIsRequiredResult.get().getLeft());
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
        return configFileToUseForUpdate.map(configFilePath -> new ConfigEnv(configurationProfile.name()));
    }

    private void deleteOldAndUnusedHostedConfigVersions(final Application application, final ConfigurationProfile configurationProfile) {

        final var versions = configVersionService.getHostedConfigurationVersions(application, configurationProfile);

        if (versions.size() > configVersionsToKeep) {
            var deleteCounter = versions.size() - configVersionsToKeep;
            final var iterator = versions.iterator();

            while (iterator.hasNext() && deleteCounter > 0) {
                appConfigFacade.deleteHostedConfigVersion(application, configurationProfile, iterator.next());
                deleteCounter--;
            }
        }
    }

    private Optional<Pair<Path, String>> checkIfAnUpdateIsRequired(final Application application, final ConfigurationProfile configurationProfile,
        final int version, final Path path) {

        var hasDiff = false;
        final var configFileAndContent = getConfigFileAndContent(path);

        if (configFileAndContent.isPresent()) {
            final var configVersionContent = appConfigFacade.getHostedConfigVersionContent(application, configurationProfile, version);
            hasDiff = !configFileAndContent.get().getRight().equals(configVersionContent);
        }

        return Optional.ofNullable(hasDiff ? configFileAndContent.get() : null);
    }

    private Optional<Pair<Path, String>> getConfigFileAndContent(final Path path) {

        Optional<Pair<Path, String>> result = Optional.empty();

        try (var environmentPaths = Files.walk(path, MAX_WALK_DEPTH)) {

            final var configFile = environmentPaths.filter(environmentPath -> environmentPath.toFile().isFile()).findFirst();
            result = Optional.ofNullable(configFile.isPresent() ? new ImmutablePair<>(configFile.get(), readString(configFile.get())) : null);

        } catch (NoSuchFileException e) {
            log.debug("ignoring NoSuchFileException");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }
}
