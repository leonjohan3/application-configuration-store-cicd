package com.myorg.spring;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import software.amazon.awssdk.services.appconfig.AppConfigClient;

@Configuration(proxyBeanMethods = false)
public class Config {

    @Bean
    @Profile("!test")
    public AppConfigClient appConfigClient() {
        return AppConfigClient.builder()
            .overrideConfiguration(conf -> conf.apiCallTimeout(Duration.ofSeconds(5)).apiCallAttemptTimeout(Duration.ofSeconds(2)))
            .build();
    }

    @Bean
    public static MethodValidationPostProcessor validationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
