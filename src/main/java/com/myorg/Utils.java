package com.myorg;

import com.myorg.model.ConfigApp;
import com.myorg.model.ConfigEnv;
import com.myorg.model.ConfigRoot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static void main(String[] args) throws IOException {
//        var bla = createConfigRoot(Path.of("src/test/resources/provided_input/"));
        normalizePath(Path.of("a/b"));
    }

    public static Path normalizePath(final Path path) {
//        path.normalize()
        var bla = path.endsWith("/");

        return Path.of("");
    }

        /*

        Files.walk(rootFolder, 1).forEach((applicationPath -> {
            if (applicationPath.toFile().isDirectory()) {
                final var configApp = new ConfigApp(applicationPath.toString());
                try {
                    Files.walk(applicationPath, 1).forEach((environmentPath -> {
                        if (environmentPath.toFile().isDirectory()) {

                            try {
                                final var configurationFileEntryCount = new AtomicInteger();

                                Files.walk(environmentPath, 1).forEach((configFilePath -> {
                                    if (configFilePath.toFile().isFile()) {
                                        if (configurationFileEntryCount.getAndIncrement() > 1) {
                                            throw new RuntimeException("Invalid configuration file entry, only one configuration file allowed: " + configFilePath);
                                        }
                                        configApp.addConfigEnv(new ConfigEnv(environmentPath.toString(), configFilePath));
                                    } else {
                                        throw new RuntimeException("Invalid configuration file entry, only files allowed: " + configFilePath);
                                    }
                                }));

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            throw new RuntimeException("Invalid environment entry: " + environmentPath);
                        }
                    }));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                configRoot.addConfigApp(configApp);
            }
        }));
         */
//        Files.walkFileTree(rootFolder, Set.of(), 1, new ApplicationFileVisitor());
//        return Files.w.walk(rootFolder, 3)
//                .filter(Files::isRegularFile)
//                .map(Path::toString)
//                .toList();
//                            final var configEnv = new ConfigEnv(environmentPath.toString());
/*

    public static class ApplicationFileVisitor implements FileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
 */
}
