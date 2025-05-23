package ru.vladuss.integrationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class IntegrationServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(IntegrationServiceApplication.class);

    public static void main(String[] args) {
        System.setProperty("server.port", String.valueOf(8082));
        SpringApplication.run(IntegrationServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .additionalInterceptors(logInterceptor())
                .build();
    }

    private ClientHttpRequestInterceptor logInterceptor() {
        return (req, body, exec) -> {
            log.debug("HTTP {} {}", req.getMethod(), req.getURI());
            return exec.execute(req, body);
        };
    }
}
