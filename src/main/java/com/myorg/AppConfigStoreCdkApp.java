package com.myorg;

import static org.apache.commons.lang3.Validate.notBlank;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class AppConfigStoreCdkApp {

    public static void main(final String[] args) throws IOException {
        final var rootFolderEnvVar = System.getenv("ROOT_CONFIG_FOLDER");
        notBlank(rootFolderEnvVar, "env var ROOT_CONFIG_FOLDER should be set");
        final var app = new App();

        new AppConfigStoreCdkStack(app, "applicationConfigurationStoreStack", StackProps.builder()
            // If you don't specify 'env', this stack will be environment-agnostic.
            // Account/Region-dependent features and context lookups will not work,
            // but a single synthesized template can be deployed anywhere.

            // Uncomment the next block to specialize this stack for the AWS Account
            // and Region that are implied by the current CLI configuration.
            /*
            .env(Environment.builder()
                    .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                    .region(System.getenv("CDK_DEFAULT_REGION"))
                    .build())
            */

            // Uncomment the next block if you know exactly what Account and Region you
            // want to deploy the stack to.
            /*
            .env(Environment.builder()
                    .account("123456789012")
                    .region("us-east-1")
                    .build())
            */

            // For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html
            .description("AWS AppConfig infra for the application configuration store Git repo: ???TODO")
            .tags(Map.of("key", "value-TODO"))
            .terminationProtection(true)
            .build(), Path.of(rootFolderEnvVar));

        app.synth();
    }
}

