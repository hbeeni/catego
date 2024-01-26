package com.been.catego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CategoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CategoApplication.class, args);
    }

}
