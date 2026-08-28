package com.viefood.viefoodidentitysvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ViefoodIdentitySvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(ViefoodIdentitySvcApplication.class, args);
    }

}
