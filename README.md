# Overview
This project enables the use of AWS AppConfig as storage for externalized configuration of applications: e.g. storing the application.yaml in the case 
of Java Spring Boot. It contains the functionality to sync an `application-configuration-store` GitHub repo with AWS AppConfig. 

# Concepts

# Also visit the other related GitHub repos
- [acs-codepipeline](<https://github.com/leonjohan3/acs-codepipeline/blob/main/README.md>)
- [application-configuration-store](<https://github.com/leonjohan3/application-configuration-store/blob/main/README.md>)

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
- [](<https://docs.aws.amazon.com/cdk/v2/guide/cli.html>)
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
- modify acs-codepipeline to accept parameters [done]
- cleanup build.gradle in all projects and remove unused dependencies
- mention predicted costs [done]
- mention cdk bootstrap requirement [done]
- mention secret with GitHub token [done]
- mention `aws sts get-caller-identity (to get the current AWS account number)` [done]
- mention `aws ec2 describe-availability-zones --output text --query 'AvailabilityZones[0].[RegionName]' (to get the current region) [done]
- complete README.md files for all 3 projects
- https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/best-practices.html
- write a section to compare with spring cloud config [done] 
- mention how to use AWS secrets using Spring AWS Cloud [done]
- change logs to expire only after 30 days [done]

# Add below rules to docs [done]
- the root folder may contain folders and files (e.g. a README.md file, or .git folder)
- all the 1st level folders (applications) should have one or more sub-folders (environments)
- all the 2nd level folders (environments) should contain only a single configuration file
