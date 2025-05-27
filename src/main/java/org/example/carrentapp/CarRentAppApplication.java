
package org.example.carrentapp;

import org.example.carrentapp.entity.*;
        import org.example.carrentapp.repository.*;

        import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.example.carrentapp.entity")
@EnableJpaRepositories("org.example.carrentapp.repository")

public class CarRentAppApplication {


    public static void main(String[] args) {
        SpringApplication.run(CarRentAppApplication.class, args);
    }

}