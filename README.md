# Overview
This project enables the use of AWS AppConfig as storage for externalized configuration of applications: e.g. storing the application.yaml in the case 
of Java Spring Boot.

# Concepts


# Welcome to your CDK Java project!

This is a blank project for CDK development with Java.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

# Resources
- [](<https://commons.apache.org/proper/commons-cli/usage.html>)
- [](<https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/>)
- [](<https://docs.spring.io/spring-framework/reference/index.html>)
- [](<https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html>)
- [](<https://commons.apache.org/proper/commons-lang/javadocs/api-release/index.html>)
- [](<>)

# Todo
- manually create .git folder if not exist before running test (because .git folder cannot be commited to GitHub) [done]
- search for and address all TODO's [done]
- if not re.match(r'^[a-zA-Z0-9][a-zA-Z0-9_\-]{1,59}$', name): [done]
- enable PMD on all files [done]
- change package `com.myorg` to `org.example` [done]
- do load/perf test with many apps+envs [done]
- remove old configuration profiles (more than 10 versions) [done]
- test with max len app and conf names [done]
- write go app to get latest version of the config (use static linking) [done]
- modify acs-codepipeline to accept parameters

# Add below rules to docs
- the root folder may contain folders and files (e.g. a README.md file, or .git folder)
- all the 1st level folders (applications) should have one or more sub-folders (environments)
- all the 2nd level folders (environments) should contain only a single configuration file
