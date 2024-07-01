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
import com.myorg.model.appconfig.ConfigurationEnvironment;
import com.myorg.model.appconfig.ConfigurationProfile;
import com.myorg.model.appconfig.Deployment;
import com.myorg.model.appconfig.DeploymentStrategy;
import com.myorg.model.appconfig.HostedConfigurationVersion;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {ConfigProfileDeployer.class, ConfigVersionService.class, ConfigProfileDeployerTests.Config.class, Config.class})
@TestPropertySource(properties = """
    app.config.group.prefix = acs
    """)
@SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
class ConfigProfileDeployerTests {

    private static final AppConfigFacade APP_CONFIG_FACADE = mock(AppConfigFacade.class);

    @Configuration(proxyBeanMethods = false)
    static class Config {

        @Bean
        public AppConfigFacade appConfigFacade() {
            return APP_CONFIG_FACADE;
        }
    }

    @Autowired
    private ConfigProfileDeployer configProfileDeployer;

    @Value("${" + ServiceConstants.APP_CONFIG_GROUP_PREFIX + "}")
    private String configGroupPrefix;

    @Test
    void shouldDeploySomeConfigProfiles() {

        // given: all data (test fixture) preparation
        final var applicationA = new Application("a", "acs/retail", "retail");
        final var applicationB = new Application("b", "acs/sales_api", "sales_api");
        final var applicationC = new Application("c", "acs/other", "other");
        final var configurationProfileA = new ConfigurationProfile("cp-a", "prod");
        final var configurationProfileB = new ConfigurationProfile("cp-b", "test");
        final var configurationProfileC = new ConfigurationProfile("cp-c", "pre-prod");
        final var configurationProfileD = new ConfigurationProfile("cp-d", "other");
        final var configurationProfileE = new ConfigurationProfile("cp-ed", "no-deployments");
        final var configurationEnvironmentA = new ConfigurationEnvironment("ce-a", "prod", "ReadyForDeployment");
        final var configurationEnvironmentB = new ConfigurationEnvironment("ce-b", "test", "ReadyForDeployment");
        final var configurationEnvironmentC = new ConfigurationEnvironment("ce-c", "pre-prod", "ReadyForDeployment");
        final var configurationEnvironmentD = new ConfigurationEnvironment("ce-d", "other", "ReadyForDeployment");
        final var configurationEnvironmentE = new ConfigurationEnvironment("ce-e", "no-deployments", "ReadyForDeployment");
        final var hostedConfigurationVersionThree = new HostedConfigurationVersion(3);
        final var hostedConfigurationVersionTwo = new HostedConfigurationVersion(2);
        final var hostedConfigurationVersionOne = new HostedConfigurationVersion(1);
        final var deploymentA = new Deployment(3, 1);
        final var deploymentB = new Deployment(2, 1);

        when(APP_CONFIG_FACADE.listDeploymentStrategies()).thenReturn(
            List.of(new DeploymentStrategy("abc", configGroupPrefix + "-all-at-once-with-no-bake-time-cdk")));
        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(applicationA, applicationB, applicationC));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationA)).thenReturn(List.of(configurationProfileA, configurationProfileB));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationB)).thenReturn(List.of(configurationProfileC));
        when(APP_CONFIG_FACADE.listConfigurationProfiles(applicationC)).thenReturn(List.of(configurationProfileD, configurationProfileE));
        when(APP_CONFIG_FACADE.listConfigurationEnvironments(applicationA)).thenReturn(List.of(configurationEnvironmentA, configurationEnvironmentB));
        when(APP_CONFIG_FACADE.listConfigurationEnvironments(applicationB)).thenReturn(List.of(configurationEnvironmentC));
        when(APP_CONFIG_FACADE.listConfigurationEnvironments(applicationC)).thenReturn(List.of(configurationEnvironmentD, configurationEnvironmentE));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileA)).thenReturn(
            List.of(hostedConfigurationVersionThree, hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationA, configurationProfileB)).thenReturn(List.of(hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationB, configurationProfileC)).thenReturn(List.of(hostedConfigurationVersionThree));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationC, configurationProfileD)).thenReturn(List.of(hostedConfigurationVersionOne));
        when(APP_CONFIG_FACADE.listHostedConfigurationVersions(applicationC, configurationProfileE)).thenReturn(List.of(hostedConfigurationVersionTwo));
        when(APP_CONFIG_FACADE.listDeployments(applicationA, configurationEnvironmentA)).thenReturn(List.of(deploymentA, deploymentB));
        when(APP_CONFIG_FACADE.listDeployments(applicationA, configurationEnvironmentB)).thenReturn(List.of(deploymentB));
        when(APP_CONFIG_FACADE.listDeployments(applicationB, configurationEnvironmentC)).thenReturn(List.of(deploymentA));
        when(APP_CONFIG_FACADE.listDeployments(applicationC, configurationEnvironmentD)).thenReturn(List.of(deploymentA));

        // when : method to be checked invocation
        final var applications = configProfileDeployer.run(configGroupPrefix);

        // then : checks and assertions
        assertThat(applications, hasSize(3));

        applications.forEach(configApp -> {
            if (configApp.name().equals("retail")) {
                assertThat(configApp.environments(), hasSize(2));
            } else {
                if (configApp.name().equals("sales_api")) {
                    assertThat(configApp.name(), is("sales_api"));
                    assertThat(configApp.environments(), hasSize(1));
                } else {
                    assertThat(configApp.name(), is("other"));
                    assertThat(configApp.environments(), hasSize(1));
                }
            }
        });

        verify(APP_CONFIG_FACADE, times(4)).deployHostedConfigVersion(any(), any(), any(), anyInt(), anyString());
    }
}
