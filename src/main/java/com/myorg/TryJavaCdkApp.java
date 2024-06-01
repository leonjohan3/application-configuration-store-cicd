package com.myorg;

import java.nio.file.Path;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class TryJavaCdkApp {

    public static void main(final String[] args) {
        /*

        final var options = new Options();
        options.addOption("r", "root-folder", true, "The root folder for the application configurations, e.g. ../application-configuration-store");
        final var parser = new DefaultParser();
        try {
            final var cmd = parser.parse(options, args);
            final var formatter = new HelpFormatter();
            formatter.setWidth(120);
            formatter.printHelp("ant", options);
            if (cmd.hasOption("r")) {
                final var rootFolder = cmd.getOptionValue("r");
                var bla = rootFolder;
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
         */

        final var rootFolder = new RootFolderArg(null).getRootFolder();
        App app = new App();

        new TryJavaCdkStack(app, "TryJavaCdkStack", StackProps.builder()
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
            .build(), Path.of(System.getenv("ROOT_FOLDER")));

        app.synth();
    }
}

