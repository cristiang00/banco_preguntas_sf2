package com.example.questionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Question Bank Saber Pro Spring Boot application.
 *
 * <p>This application implements clean persistence with Spring Data JPA and H2
 * following a layered architecture with complete decoupling of business domain
 * entities from JPA persistence records.</p>
 */
@SpringBootApplication
public class QuestionBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionBankApplication.class, args);
    }
}
