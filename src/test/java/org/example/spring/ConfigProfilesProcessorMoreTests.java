package org.example.spring;

import static org.example.constants.ServiceConstants.GROUP_PREFIX;
import static org.example.constants.ServiceConstants.ROOT_CONFIG_FOLDER;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.List;
import org.example.model.appconfig.Application;
import org.example.model.appconfig.ConfigurationProfile;
import org.example.model.appconfig.HostedConfigurationVersion;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {ConfigProfilesProcessor.class, ConfigVersionService.class, ConfigProfilesProcessorMoreTests.Config.class, Config.class})
@TestPropertySource(properties = """
    app.config.group.prefix = acs
    app.config.root.config.folder = src/test/resources/invalid_config_folder_structures/with_folder_in_place_of_a_configuration_file
    """)
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class ConfigProfilesProcessorMoreTests {

    private static final AppConfigFacade APP_CONFIG_FACADE = mock(AppConfigFacade.class);

    @Autowired
    private ConfigProfilesProcessor configProfilesProcessor;

    @Value("${" + ROOT_CONFIG_FOLDER + "}")
    private Path rootConfigFolder;

    @Value("${" + GROUP_PREFIX + "}")
    private String configGroupPrefix;

    @Configuration(proxyBeanMethods = false)
    public static class Config {

        @Bean
        public AppConfigFacade appConfigFacade() {
            return APP_CONFIG_FACADE;
        }
    }

    @Test
    void shouldAllowMissingConfigFile() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/my_application", "my_application");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "my_environment");
        final var configurationVersionTwo = new HostedConfigurationVersion(1);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(List.of(configurationVersionTwo));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        MatcherAssert.assertThat(applications, hasSize(0));
    }
}
