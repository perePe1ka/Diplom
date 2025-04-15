package ru.vladuss.documentgeneration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DocumentGenerationApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", String.valueOf(8081));
        SpringApplication.run(DocumentGenerationApplication.class, args);
    }
}
