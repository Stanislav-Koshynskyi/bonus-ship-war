package org.example.server.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TestWebSocketController {
    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String testMassage(String massage){
        return "Отримано - " + massage;
    }
}
