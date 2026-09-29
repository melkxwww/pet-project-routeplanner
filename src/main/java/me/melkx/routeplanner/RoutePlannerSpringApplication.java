package me.melkx.routeplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RoutePlannerSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoutePlannerSpringApplication.class, args);
    }
}
