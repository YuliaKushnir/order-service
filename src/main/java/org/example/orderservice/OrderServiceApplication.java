package org.example.orderservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.orderservice.config.PricingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PricingProperties.class)
public class OrderServiceApplication {
    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        System.setProperty("cloudinary_cloud-name", dotenv.get("cloudinary_cloud-name"));
        System.setProperty("cloudinary_api-key", dotenv.get("cloudinary_api-key"));
        System.setProperty("cloudinary_api_secret", dotenv.get("cloudinary_api_secret"));

        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
