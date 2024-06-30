package com.myorg.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myorg.constants.ServiceConstants;
import com.myorg.model.appconfig.Application;
import com.myorg.model.appconfig.ConfigurationProfile;
import com.myorg.model.appconfig.HostedConfigurationVersion;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {ConfigProfilesProcessor.class, ConfigProfilesProcessorTests.Config.class, Config.class})
@TestPropertySource(properties = """
    app.config.group.prefix = acs
    app.config.root.config.folder = src/test/resources/provided_input
    hosted.config.versions.to.keep=1
    """)
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class ConfigProfilesProcessorTests {

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
    void shouldFindSomeDiffs() {
        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/retail", "retail");
        final var applicationB = new Application("b", "acs/sales_api", "sales_api");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "prod");
        final var configurationProfileB = new ConfigurationProfile("cp-b", "test");
        final var configurationProfileC = new ConfigurationProfile("cp-c", "pre-prod");
        final var hostedConfigurationVersionOne = new HostedConfigurationVersion(1);
        final var hostedConfigurationVersionTwo = new HostedConfigurationVersion(2);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA, applicationB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA, configurationProfileB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationB)).thenReturn(List.of(configurationProfileC));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(
            List.of(hostedConfigurationVersionOne, hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileB)).thenReturn(List.of(hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationB, configurationProfileC)).thenReturn(List.of(hostedConfigurationVersionOne));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileB, 2)).thenReturn("debug: false");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationB, configurationProfileC, 1)).thenReturn("debug: all");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        assertThat(applications, hasSize(2));

        applications.forEach(configApp -> {
            if (configApp.name().equals("retail")) {
                assertThat(configApp.environments(), hasSize(2));
            } else {
                assertThat(configApp.name(), is("sales_api"));
                assertThat(configApp.environments(), hasSize(1));
            }
        });
    }

    @Test
    void shouldAllowMissingConfigFile() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/retail", "retail");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "pre-prod");
        final var hostedConfigurationVersionTwo = new HostedConfigurationVersion(2);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(List.of(hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        assertThat(applications, hasSize(0));
    }

    @Test
    void shouldAllowMissingHostedVersion() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/retail", "retail");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "pre-prod");

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        assertThat(applications, hasSize(0));
    }

    @Test
    void shouldFindNoDiffs() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/sales_api", "sales_api");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "pre-prod");
        final var hostedConfigurationVersionTwo = new HostedConfigurationVersion(1);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(List.of(hostedConfigurationVersionTwo));
        final var configFileContent = """
            spring:
              mvc:
                servlet:
                  load-on-startup: 1
                        
              main:
                banner-mode: off
            """;
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 1)).thenReturn(configFileContent);

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        assertThat(applications, hasSize(0));
    }

    @Test
    void shouldUpdateSomeConfigProfiles() {
        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/retail", "retail");
        final var applicationB = new Application("b", "acs/sales_api", "sales_api");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "prod");
        final var configurationProfileB = new ConfigurationProfile("cp-b", "test");
        final var configurationProfileC = new ConfigurationProfile("cp-c", "pre-prod");
        final var configurationProfileD = new ConfigurationProfile("cp-d", "dev");
        final var hostedConfigurationVersionOne = new HostedConfigurationVersion(1);
        final var hostedConfigurationVersionTwo = new HostedConfigurationVersion(2);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA, applicationB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA, configurationProfileB, configurationProfileD));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationB)).thenReturn(List.of(configurationProfileC));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(
            List.of(hostedConfigurationVersionOne, hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileB)).thenReturn(Collections.emptyList());
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileD)).thenReturn(Collections.emptyList());
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationB, configurationProfileC)).thenReturn(List.of(hostedConfigurationVersionOne));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileB, 2)).thenReturn("debug: false");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationB, configurationProfileC, 1)).thenReturn("debug: all");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, true);

        // then : checks and assertions
        assertThat(applications, hasSize(2));

        applications.forEach(configApp -> {
            if (configApp.name().equals("retail")) {
                assertThat(configApp.environments(), hasSize(2));
            } else {
                assertThat(configApp.name(), is("sales_api"));
                assertThat(configApp.environments(), hasSize(1));
            }
        });

        verify(APP_CONFIG_FACADE, times(3)).createHostedConfigVersion(any(), any(), anyString());
        verify(APP_CONFIG_FACADE).deleteHostedConfigVersion(any(), any(), anyInt());
    }
}
