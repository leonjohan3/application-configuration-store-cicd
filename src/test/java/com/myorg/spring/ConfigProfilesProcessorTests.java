package com.myorg.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.myorg.constants.ServiceConstants;
import com.myorg.model.appconfig.Application;
import java.nio.file.Path;
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
    app.config.application.prefix = acs
    app.config.root.config.folder = src/test/resources/provided_input
    """)
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

    @Value("${" + ServiceConstants.APP_CONFIG_APPLICATION_PREFIX + "}")
    private String applicationPrefix;

    @Test
    void shouldA() {
        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(List.of(new Application("a", "aa"), new Application("b", "bb")));
        final var applications = configProfilesProcessor.run(rootConfigFolder, applicationPrefix);
        assertThat(applications, hasSize(0));
    }

    @Test
    void shouldB() {
        when(APP_CONFIG_FACADE.listApplications(anyString())).thenReturn(
            List.of(new Application("a", "aa"), new Application("b", "bb"), new Application("c", "cc")));
        final var applications = configProfilesProcessor.run(rootConfigFolder, applicationPrefix);
        assertThat(applications, hasSize(0));
    }
}
