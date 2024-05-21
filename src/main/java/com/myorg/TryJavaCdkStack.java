package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.appconfig.Application;
import software.amazon.awscdk.services.appconfig.CfnConfigurationProfile;
import software.amazon.awscdk.services.appconfig.DeploymentStrategy.Builder;
import software.amazon.awscdk.services.appconfig.Environment;
import software.amazon.awscdk.services.appconfig.DeploymentStrategy;
import software.amazon.awscdk.services.appconfig.RolloutStrategy;
import software.amazon.awscdk.services.appconfig.RolloutStrategyProps;
import software.constructs.Construct;

public class TryJavaCdkStack extends Stack {

    public TryJavaCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public TryJavaCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
//         final Queue queue = Queue.Builder.create(this, "TryJavaCdkQueue")
//                 .visibilityTimeout(Duration.seconds(300))
//                 .build();

        var aaa = DeploymentStrategy.Builder.create(this, "TryJavaCdkDeploymentStrategy")
            .rolloutStrategy(RolloutStrategy.linear(RolloutStrategyProps.builder()
                    .deploymentDuration(Duration.minutes(0))
                    .finalBakeTime(Duration.minutes(0))
                    .growthFactor(100)
                .build()))
            .description("DeploymentStrategy for application configuration store that deploys without any delay")
            .deploymentStrategyName("acs-all-at-once-with-no-bake-time-cdk")
            .build();

        var applicationName = "myApp";
        var application = Application.Builder.create(this, "TryJavaCdkAppConfig")
            .applicationName("acs/myApp")
            .description(applicationName)
            .build();

        var environmentName = "dev";

        var environment = Environment.Builder.create(this, "TryJavaCdkAppConfigEnv")
            .application(application)
            .environmentName(environmentName)
            //       Description: '`{{ environment.name }}` environment of the `{{ application.name }}` application'
            .description(String.format("`%s` environment of the `%s` application", environmentName, applicationName))
            .build();

        var configurationProfile = CfnConfigurationProfile.Builder.create(this, "TryJavaCdkAppConfigProfile")
            .applicationId(application.getApplicationId())
            .locationUri("hosted")
            .name(environmentName)
            .type("AWS.Freeform")
            .build();
    }
}
