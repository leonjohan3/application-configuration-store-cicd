package org.example;

import static org.example.constants.ServiceConstants.GROUP_PREFIX;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.ConfigProfilesDeployer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@UtilityClass
@Slf4j
public class DeployConfigProfile {

    public static void main(final String[] args) {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.scan("org.example.spring");
            ctx.refresh();
            final var configGroupPrefix = ctx.getEnvironment().getRequiredProperty(GROUP_PREFIX);
            final var configApps = ctx.getBean(ConfigProfilesDeployer.class).run(configGroupPrefix);

            configApps.forEach(configApp -> configApp.environments().forEach(
                configEnv -> log.info("Successfully deployed the `{}` environment of the `{}` application in AWS AppConfig"
                        + " - the application likely requires a re-start to activate the newly deployed configuration",
                    configEnv.name(), configGroupPrefix + "/" + configApp.name())));
        }
    }
}
