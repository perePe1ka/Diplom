package ru.vladuss.informantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InformantServiceApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8083");
        SpringApplication.run(InformantServiceApplication.class, args);
    }

}
