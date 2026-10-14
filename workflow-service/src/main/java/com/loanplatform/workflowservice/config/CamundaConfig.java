package com.loanplatform.workflowservice.config;

import io.camunda.client.CamundaClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Bean
    public CommandLineRunner deployProcesses(CamundaClient client){
        return args -> {
            client.newDeployResourceCommand()
                    .addResourceFromClasspath("processes/loan-origination.bpmn")
                    .addResourceFromClasspath("processes/Manager-Review.form")
                    .send()
                    .join();

            System.out.println("Loan origination process and manager review form deployed successfully.");
        };
    }

}
