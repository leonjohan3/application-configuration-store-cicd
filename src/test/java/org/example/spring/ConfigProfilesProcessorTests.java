package org.example.spring;

import static org.example.constants.ServiceConstants.GROUP_PREFIX;
import static org.example.constants.ServiceConstants.ROOT_CONFIG_FOLDER;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.Collections;
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

@SpringJUnitConfig(classes = {ConfigProfilesProcessor.class, ConfigVersionService.class, ConfigProfilesProcessorTests.Config.class, Config.class})
@TestPropertySource(properties = """
    app.config.group.prefix = acs
    app.config.root.config.folder = src/test/resources/provided_input
    hosted.config.versions.to.keep=1
    """)
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class ConfigProfilesProcessorTests {

    private static final String ACS_RETAIL = "acs/retail";
    private static final String RETAIL = "retail";
    private static final String SALES_API = "sales_api";
    private static final String CP_A = "cp-a";
    private static final String PRE_PROD = "pre-prod";

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
    void shouldFindSomeDiffs() {
        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", ACS_RETAIL, RETAIL);
        final var applicationB = new Application("b", "acs/sales_api", SALES_API);
        final var configurationProfileA = new ConfigurationProfile(CP_A, "prod");
        final var configurationProfileB = new ConfigurationProfile("cp-b", "test");
        final var configurationProfileC = new ConfigurationProfile("cp-c", PRE_PROD);
        final var configurationVersionOne = new HostedConfigurationVersion(1);
        final var configurationVersionTwo = new HostedConfigurationVersion(2);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA, applicationB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA, configurationProfileB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationB)).thenReturn(List.of(configurationProfileC));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(
            List.of(configurationVersionOne, configurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileB)).thenReturn(List.of(configurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationB, configurationProfileC)).thenReturn(List.of(configurationVersionOne));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileB, 2)).thenReturn("debug: false");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationB, configurationProfileC, 1)).thenReturn("debug: all");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        MatcherAssert.assertThat(applications, hasSize(2));

        applications.forEach(configApp -> {
            if (RETAIL.equals(configApp.name())) {
                MatcherAssert.assertThat(configApp.environments(), hasSize(2));
            } else {
                MatcherAssert.assertThat(configApp.name(), is(SALES_API));
                MatcherAssert.assertThat(configApp.environments(), hasSize(1));
            }
        });
    }

    @Test
    void shouldAllowMissingConfigFile() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", ACS_RETAIL, RETAIL);
        final var configurationProfileA = new ConfigurationProfile(CP_A, PRE_PROD);
        final var configurationVersionTwo = new HostedConfigurationVersion(2);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(List.of(configurationVersionTwo));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        MatcherAssert.assertThat(applications, hasSize(0));
    }

    @Test
    void shouldAllowMissingHostedVersion() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", ACS_RETAIL, RETAIL);
        final var configurationProfileA = new ConfigurationProfile(CP_A, PRE_PROD);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, false);

        // then : checks and assertions
        MatcherAssert.assertThat(applications, hasSize(0));
    }

    @Test
    void shouldFindNoDiffs() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/sales_api", SALES_API);
        final var configurationProfileA = new ConfigurationProfile(CP_A, PRE_PROD);
        final var configurationVersionTwo = new HostedConfigurationVersion(1);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(List.of(configurationVersionTwo));
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
        MatcherAssert.assertThat(applications, hasSize(0));
    }

    @Test
    void shouldUpdateSomeConfigProfiles() {
        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", ACS_RETAIL, RETAIL);
        final var applicationB = new Application("b", "acs/sales_api", SALES_API);
        final var configurationProfileA = new ConfigurationProfile(CP_A, "prod");
        final var configurationProfileB = new ConfigurationProfile("cp-b", "test");
        final var configurationProfileC = new ConfigurationProfile("cp-c", PRE_PROD);
        final var configurationProfileD = new ConfigurationProfile("cp-d", "dev");
        final var configurationVersionOne = new HostedConfigurationVersion(1);
        final var configurationVersionTwo = new HostedConfigurationVersion(2);

        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA, applicationB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA, configurationProfileB, configurationProfileD));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationB)).thenReturn(List.of(configurationProfileC));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(
            List.of(configurationVersionOne, configurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileB)).thenReturn(Collections.emptyList());
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileD)).thenReturn(Collections.emptyList());
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationB, configurationProfileC)).thenReturn(List.of(configurationVersionOne));
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileA, 2)).thenReturn("debug: true");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationA, configurationProfileB, 2)).thenReturn("debug: false");
        when(APP_CONFIG_FACADE.getHostedConfigVersionContent(applicationB, configurationProfileC, 1)).thenReturn("debug: all");

        // when : method to be checked invocation
        final var applications = configProfilesProcessor.run(rootConfigFolder, configGroupPrefix, true);

        // then : checks and assertions
        MatcherAssert.assertThat(applications, hasSize(2));

        applications.forEach(configApp -> {
            if (RETAIL.equals(configApp.name())) {
                MatcherAssert.assertThat(configApp.environments(), hasSize(2));
            } else {
                MatcherAssert.assertThat(configApp.name(), is(SALES_API));
                MatcherAssert.assertThat(configApp.environments(), hasSize(1));
            }
        });

        verify(APP_CONFIG_FACADE, times(3)).createHostedConfigVersion(any(), any(), anyString());
        verify(APP_CONFIG_FACADE).deleteHostedConfigVersion(any(), any(), anyInt());
    }
}
