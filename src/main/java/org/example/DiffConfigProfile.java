package org.example;

import static org.example.constants.ServiceConstants.APP_CONFIG_GROUP_PREFIX;
import static org.example.constants.ServiceConstants.APP_CONFIG_ROOT_CONFIG_FOLDER;

import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.ConfigProfilesProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class DiffConfigProfile {

    public static void main(final String[] args) {
        try (final var ctx = new AnnotationConfigApplicationContext()) {
            ctx.scan("org.example.spring");
            ctx.refresh();
            final var configGroupPrefix = ctx.getEnvironment().getRequiredProperty(APP_CONFIG_GROUP_PREFIX);
            final var configApps = ctx.getBean(ConfigProfilesProcessor.class).run(Path.of(ctx.getEnvironment().getRequiredProperty(APP_CONFIG_ROOT_CONFIG_FOLDER)),
                configGroupPrefix, false);

            configApps.forEach(configApp -> configApp.environments().forEach(
                configEnv -> log.info("The configuration of the `{}` environment of the `{}` application will be updated in AWS AppConfig",
                    configEnv.name(), configGroupPrefix + "/" + configApp.name())));
        }
    }
}
