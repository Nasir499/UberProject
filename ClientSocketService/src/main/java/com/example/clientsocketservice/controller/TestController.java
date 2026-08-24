package com.example.clientsocketservice.controller;

import com.example.clientsocketservice.dto.TestRequest;
import com.example.clientsocketservice.dto.TestResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class TestController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    public TestController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @MessageMapping("/ping")
    @SendTo("/topic/ping")
    public TestResponse pingCheck(TestRequest message){
        System.out.println("Recieved message from client: " + message.getData());
      return TestResponse.builder().data("Recieved").build();
    }

//    @Scheduled(fixedRate = 1000)
//    public String sendPeriodicMessage(){
//        simpMessagingTemplate.convertAndSend("/topic/s", "Hello from server");
//        return "Hello from server";
//    }


}
