package com.myorg;

import static com.myorg.constants.ServiceConstants.APP_CONFIG_GROUP_PREFIX;

import com.myorg.spring.ConfigProfileDeployer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class DeployConfigProfile {

    public static void main(final String[] args) {
        try (final var ctx = new AnnotationConfigApplicationContext()) {
            ctx.scan("com.myorg.spring");
            ctx.refresh();
            final var configGroupPrefix = ctx.getEnvironment().getRequiredProperty(APP_CONFIG_GROUP_PREFIX);
            final var configApps = ctx.getBean(ConfigProfileDeployer.class).run(configGroupPrefix);

            configApps.forEach(configApp -> configApp.environments().forEach(
                configEnv -> log.info("Successfully deployed the `{}` environment of the `{}` application in AWS AppConfig"
                        + " - the application likely requires a re-start to activate the newly deployed configuration",
                    configEnv.name(), configGroupPrefix + "/" + configApp.name())));
        }
    }
}
