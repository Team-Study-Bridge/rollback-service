package org.example.rollbackservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RollbackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RollbackServiceApplication.class, args);
    }

}
