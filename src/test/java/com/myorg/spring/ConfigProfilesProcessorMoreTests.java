package com.myorg.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.myorg.constants.ServiceConstants;
import com.myorg.model.appconfig.Application;
import com.myorg.model.appconfig.ConfigurationProfile;
import com.myorg.model.appconfig.HostedConfigurationVersion;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {ConfigProfilesProcessor.class, ConfigProfilesProcessorMoreTests.Config.class, Config.class})
@TestPropertySource(properties = """
    app.config.group.prefix = acs
    app.config.root.config.folder = src/test/resources/invalid_config_folder_structures/with_folder_in_place_of_a_configuration_file
    """)
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class ConfigProfilesProcessorMoreTests {

    private static final AppConfigFacade APP_CONFIG_FACADE = mock(AppConfigFacade.class);

    @Configuration(proxyBeanMethods = false)
    static class Config {

        @Bean
        public AppConfigFacade appConfigFacade() {
            return APP_CONFIG_FACADE;
        }
    }

    @Autowired
    private ConfigProfilesProcessor configProfilesProcessor;

    @Value("${" + ServiceConstants.APP_CONFIG_ROOT_CONFIG_FOLDER + "}")
    private Path rootConfigFolder;

    @Value("${" + ServiceConstants.APP_CONFIG_GROUP_PREFIX + "}")
    private String configGroupPrefix;

    @Test
    void shouldAllowMissingConfigFile() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/my_application", "my_application");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "my_environment");
        final var hostedConfigurationVersionTwo = new HostedConfigurationVersion(1);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(List.of(hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        assertThat(applications, hasSize(0));
    }
}
