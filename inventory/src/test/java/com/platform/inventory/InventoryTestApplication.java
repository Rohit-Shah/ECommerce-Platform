package com.platform.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.platform.inventory")
@EnableJpaRepositories(basePackages = "com.platform.inventory")
public class InventoryTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryTestApplication.class);
    }
}
