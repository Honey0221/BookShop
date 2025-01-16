package com.bbook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SlackConfig {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    @Bean
    public WebClient slackWebClient() {
        return WebClient.builder()
                .baseUrl(webhookUrl)
                .build();
    }
}