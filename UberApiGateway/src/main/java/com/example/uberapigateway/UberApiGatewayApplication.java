package com.example.uberapigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UberApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(UberApiGatewayApplication.class, args);
    }
}
