package org.example.spring;

import static org.example.constants.ServiceConstants.GROUP_PREFIX;
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
import java.util.List;
import org.example.model.appconfig.Application;
import org.example.model.appconfig.ConfigurationEnvironment;
import org.example.model.appconfig.ConfigurationProfile;
import org.example.model.appconfig.Deployment;
import org.example.model.appconfig.DeploymentStrategy;
import org.example.model.appconfig.HostedConfigurationVersion;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {ConfigProfilesDeployer.class, ConfigVersionService.class, ConfigProfilesDeployerTests.Config.class, Config.class})
@TestPropertySource(properties = """
    app.config.group.prefix = acs
    """)
@SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class ConfigProfilesDeployerTests {

    private static final String OTHER = "other";
    private static final String READY_FOR_DEPLOYMENT = "ReadyForDeployment";
    private static final String RETAIL = "retail";

    private static final AppConfigFacade APP_CONFIG_FACADE = mock(AppConfigFacade.class);
    public static final String SALES_API = "sales_api";

    @Autowired
    private ConfigProfilesDeployer configProfilesDeployer;

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
    void shouldDeploySomeConfigProfiles() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/retail", RETAIL);
        final var applicationB = new Application("b", "acs/sales_api", SALES_API);
        final var applicationC = new Application("c", "acs/other", OTHER);
        final var configurationProfileA = new ConfigurationProfile("cp-a", "prod");
        final var configurationProfileB = new ConfigurationProfile("cp-b", "test");
        final var configurationProfileC = new ConfigurationProfile("cp-c", "pre-prod");
        final var configurationProfileD = new ConfigurationProfile("cp-d", OTHER);
        final var configurationProfileE = new ConfigurationProfile("cp-ed", "no-deployments");
        final var configEnvironmentA = new ConfigurationEnvironment("ce-a", "prod", READY_FOR_DEPLOYMENT);
        final var configEnvironmentB = new ConfigurationEnvironment("ce-b", "test", READY_FOR_DEPLOYMENT);
        final var configEnvironmentC = new ConfigurationEnvironment("ce-c", "pre-prod", READY_FOR_DEPLOYMENT);
        final var configEnvironmentD = new ConfigurationEnvironment("ce-d", OTHER, READY_FOR_DEPLOYMENT);
        final var configEnvironmentE = new ConfigurationEnvironment("ce-e", "no-deployments", READY_FOR_DEPLOYMENT);
        final var configVersionThree = new HostedConfigurationVersion(3);
        final var configVersionTwo = new HostedConfigurationVersion(2);
        final var configVersionOne = new HostedConfigurationVersion(1);
        final var deploymentA = new Deployment(3, 1);
        final var deploymentB = new Deployment(2, 1);

        when(APP_CONFIG_FACADE.listDeploymentStrategies()).thenReturn(
            List.of(new DeploymentStrategy("abc", configGroupPrefix + "-all-at-once-with-no-bake-time-cdk")));
        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA, applicationB, applicationC));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA, configurationProfileB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationB)).thenReturn(List.of(configurationProfileC));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationC)).thenReturn(List.of(configurationProfileD, configurationProfileE));
        when(APP_CONFIG_FACADE.listConfigurationEnvironments(applicationA)).thenReturn(List.of(configEnvironmentA, configEnvironmentB));
        when(APP_CONFIG_FACADE.listConfigurationEnvironments(applicationB)).thenReturn(List.of(configEnvironmentC));
        when(APP_CONFIG_FACADE.listConfigurationEnvironments(applicationC)).thenReturn(List.of(configEnvironmentD, configEnvironmentE));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(
            List.of(configVersionThree, configVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileB)).thenReturn(List.of(configVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationB, configurationProfileC)).thenReturn(List.of(configVersionThree));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationC, configurationProfileD)).thenReturn(List.of(configVersionOne));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationC, configurationProfileE)).thenReturn(List.of(configVersionTwo));
        when(APP_CONFIG_FACADE.listDeployments(applicationA, configEnvironmentA)).thenReturn(List.of(deploymentA, deploymentB));
        when(APP_CONFIG_FACADE.listDeployments(applicationA, configEnvironmentB)).thenReturn(List.of(deploymentB));
        when(APP_CONFIG_FACADE.listDeployments(applicationB, configEnvironmentC)).thenReturn(List.of(deploymentA));
        when(APP_CONFIG_FACADE.listDeployments(applicationC, configEnvironmentD)).thenReturn(List.of(deploymentA));

        // when : method to be checked invocation
        final var applications = configProfilesDeployer.run(configGroupPrefix);

        // then : checks and assertions
        MatcherAssert.assertThat(applications, hasSize(3));

        applications.forEach(configApp -> {
            if (RETAIL.equals(configApp.name())) {
                MatcherAssert.assertThat(configApp.environments(), hasSize(2));
            } else {
                if (SALES_API.equals(configApp.name())) {
                    MatcherAssert.assertThat(configApp.name(), is(SALES_API));
                    MatcherAssert.assertThat(configApp.environments(), hasSize(1));
                } else {
                    MatcherAssert.assertThat(configApp.name(), is(OTHER));
                    MatcherAssert.assertThat(configApp.environments(), hasSize(1));
                }
            }
        });

        verify(APP_CONFIG_FACADE, times(4)).deployHostedConfigVersion(any(), any(), any(), anyInt(), anyString());
    }
}
