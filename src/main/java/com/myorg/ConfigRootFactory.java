package com.myorg;

import static com.myorg.constants.ServiceConstants.MAX_WALK_DEPTH;

import com.myorg.exception.ConfigRootException;
import com.myorg.model.ConfigApp;
import com.myorg.model.ConfigEnv;
import com.myorg.model.ConfigRoot;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigRootFactory {

    private static final int MAX_CONFIG_FILE_COUNT = 1;

    public static ConfigRoot createConfigRoot(final Path rootFolder) throws IOException {
        Objects.requireNonNull(rootFolder, "rootFolder must not be null");
        final var applications = new HashSet<ConfigApp>();

        try (var applicationPaths = Files.walk(rootFolder, MAX_WALK_DEPTH)) {
            applicationPaths.filter(
                    applicationPath -> !applicationPath.equals(rootFolder) && !applicationPath.endsWith(Path.of(".git")) && applicationPath.toFile().isDirectory())
                .forEach(applicationPath -> {

                    final var environments = new HashSet<ConfigEnv>();

                    try (var environmentPaths = Files.walk(applicationPath, MAX_WALK_DEPTH)) {
                        environmentPaths.filter(environmentPath -> !environmentPath.equals(applicationPath))
                            .forEach(environmentPath -> addConfigEnv(applicationPath, environmentPath, environments));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    applications.add(ConfigApp.builder()
                        .name(applicationPath.getFileName().toString())
                        .environments(environments)
                        .build());
                });
        }
        return ConfigRoot.builder()
            .rootFolder(rootFolder.toString())
            .applications(applications)
            .build();
    }

    private static void addConfigEnv(final Path applicationPath, final Path environmentPath, final Set<ConfigEnv> environments) {
        Objects.requireNonNull(applicationPath, "applicationPath must not be null");
        Objects.requireNonNull(environmentPath, "environmentPath must not be null");
        Objects.requireNonNull(environments, "environments must not be null");

        if (environmentPath.toFile().isDirectory()) {
            final var configFileEntryCount = new AtomicInteger();

            try (var configFilePaths = Files.walk(environmentPath, MAX_WALK_DEPTH)) {

                configFilePaths.filter(configFilePath -> !configFilePath.equals(environmentPath)).forEach(configFilePath -> {

                    if (configFilePath.toFile().isFile()) {
                        environments.add(ConfigEnv.builder()
                            .name(environmentPath.getFileName().toString())
                            .build());
                    } else {
                        throw new ConfigRootException("Invalid configuration file entry, only files are allowed: " + configFilePath);
                    }
                    if (configFileEntryCount.incrementAndGet() > MAX_CONFIG_FILE_COUNT) {
                        throw new ConfigRootException("Invalid configuration file entry, only one configuration file allowed: " + configFilePath);
                    }
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            throw new ConfigRootException("Invalid configuration file entry, only folders are allowed: " + environmentPath);
        }
    }
}
