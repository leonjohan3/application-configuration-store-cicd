# Overview
Go command line utility to get the latest configuration (e.g. application.yaml), based on the application and environment names, from AWS AppConfig.

# Background
This standalone command line utility can be used e.g. in the Java Spring Boot startup script (in the Dockerfile if using Docker) to get the latest configuration 
(e.g. application.yaml) like this:
```bash
#!/bin/bash

mkdir -p config
./getLatestConfig -a acs/sales_api -e prod -o config/application.yaml
exec java -Dspring.profiles.active=prod -Duser.home=/tmp -Xms768m -Xmx768m -cp app/BOOT-INF/classes:app/BOOT-INF/lib/* org.example.MyApplication
```

# Building (suggestion: use AWS CloudShell)
Change directory to this folder, then run `GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build getLatestConfig.go`

# Prerequisites
- Install go, e.g. `sudo yum install golang`

# Usage
`./getLatestConfig -a acs/application_name -e environment -o config/application.yaml`

```text
Usage of getLatestConfig:
-a acs/sales_api
   application name (with the configuration group prefix, e.g. acs/sales_api)
-e prod
   environment name (e.g. prod)
-o config/application.yaml
   output path (e.g. config/application.yaml)
```

# Notes
- The application_name should include the configuration group prefix, e.g. `-a acs/sales_api` and needs to match the application name in AWS AppConfig.
- The environment is e.g. `prod`, `test`, `dev`, etc. and needs to match the ConfigurationProfile in AWS AppConfig.
- The output option specifies where the configuration file (e.g. application.yaml) should be created.
- If the configuration file needs to be created inside a folder, e.g. `config`, the folder must already exist.
- Golang was selected because it provides a standalone executable that adds less than 12MB to the Docker image.

# Resources
- [AWS SDK for Go V2](<https://aws.github.io/aws-sdk-go-v2/docs/getting-started/>)
- [AWS appconfigdata](<https://pkg.go.dev/github.com/aws/aws-sdk-go-v2/service/appconfigdata@v1.16.1>)
- [SDK for Go V2 code examples](<https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/gov2>)
