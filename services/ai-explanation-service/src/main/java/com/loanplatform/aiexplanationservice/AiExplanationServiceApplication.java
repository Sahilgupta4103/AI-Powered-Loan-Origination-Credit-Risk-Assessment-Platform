package com.loanplatform.aiexplanationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(
        excludeName = {
                "org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiEmbeddingConnectionAutoConfiguration",
                "org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingAutoConfiguration"
        }
)
public class AiExplanationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiExplanationServiceApplication.class, args);
    }

}
