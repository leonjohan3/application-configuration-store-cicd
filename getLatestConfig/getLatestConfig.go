package main

import (
	"context"
	"os"
	"flag"
	"fmt"
	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/service/appconfigdata"
)

func main() {
	var application_name string
	var environment_name string
	var output_path string
	
	flag.StringVar(&application_name, "a", "", "application name (with the configuration group prefix, e.g. `acs/sales_api`)")
	flag.StringVar(&environment_name, "e", "", "environment name (e.g. `prod`)")
	flag.StringVar(&output_path, "o", "", "output path (e.g. `config/application.yaml`)")
	flag.Parse()

	if application_name == "" || environment_name == "" || output_path == "" {
		flag.Usage()
		os.Exit(1)
	}
	sdkConfig, err := config.LoadDefaultConfig(context.TODO())
	
	if err != nil {
		fmt.Println("Couldn't load default configuration. Have you set up your AWS account?")
		fmt.Println(err)
		os.Exit(2)
	}
	appConfigDataClient := appconfigdata.NewFromConfig(sdkConfig)
	var pollInterval int32 = 60

	result, err := appConfigDataClient.StartConfigurationSession(context.TODO(), &appconfigdata.StartConfigurationSessionInput{
		ApplicationIdentifier: aws.String(application_name),
		ConfigurationProfileIdentifier: aws.String(environment_name),
		EnvironmentIdentifier: aws.String(environment_name),
		RequiredMinimumPollIntervalInSeconds: &pollInterval,
	})

	if err != nil {
		fmt.Printf("Couldn't get session. Here's why: %v\n", err)
		os.Exit(3)
	}

	getResult, getErr := appConfigDataClient.GetLatestConfiguration(context.TODO(), &appconfigdata.GetLatestConfigurationInput{
		ConfigurationToken: result.InitialConfigurationToken,
	})

	if getErr != nil {
		fmt.Printf("Couldn't get latest config. Here's why: %v\n", getErr)
		os.Exit(4)
	} else {
		err := os.WriteFile(output_path, getResult.Configuration, 0600)

		if err != nil { 
			fmt.Printf("Couldn't write content to file. Here's why: %v\n", err)
			os.Exit(5)
		}
	}
	
}
