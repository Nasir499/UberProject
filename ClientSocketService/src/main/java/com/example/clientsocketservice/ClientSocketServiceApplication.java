package com.example.clientsocketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.example.clientsocketservice.models")
public class ClientSocketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientSocketServiceApplication.class, args);
    }

}
