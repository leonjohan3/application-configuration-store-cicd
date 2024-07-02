package org.example;

import static org.example.constants.ServiceConstants.GROUP_PREFIX;
import static org.example.constants.ServiceConstants.ROOT_CONFIG_FOLDER;

import java.nio.file.Path;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.ConfigProfilesProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@UtilityClass
@Slf4j
public class DiffConfigProfile {

    public static void main(final String[] args) {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.scan("org.example.spring");
            ctx.refresh();
            final var configGroupPrefix = ctx.getEnvironment().getRequiredProperty(GROUP_PREFIX);
            final var configApps = ctx.getBean(ConfigProfilesProcessor.class).run(Path.of(ctx.getEnvironment().getRequiredProperty(ROOT_CONFIG_FOLDER)),
                configGroupPrefix, false);

            configApps.forEach(configApp -> configApp.environments().forEach(
                configEnv -> log.info("The configuration of the `{}` environment of the `{}` application will be updated in AWS AppConfig",
                    configEnv.name(), configGroupPrefix + "/" + configApp.name())));
        }
    }
}
