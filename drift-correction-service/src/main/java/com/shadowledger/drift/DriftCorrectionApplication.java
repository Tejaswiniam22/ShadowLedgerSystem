package com.shadowledger.drift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class DriftCorrectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriftCorrectionApplication.class, args);
    }
}
