package com.maths22.ftc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Created by Jacob on 2/1/2017.
 */
@SpringBootApplication
public class ScoringResults extends SpringBootServletInitializer {
    private static String[] args;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ScoringResults.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ScoringResults.class, args);
    }

}
