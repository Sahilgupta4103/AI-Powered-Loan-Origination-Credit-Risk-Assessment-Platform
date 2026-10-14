package com.loanplatform.loanservice.config;


import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanPlatformOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Origination Platform - Loan Service API")
                        .version("v1")
                        .description("REST APIs managing loan applications")
                );
    }

}
