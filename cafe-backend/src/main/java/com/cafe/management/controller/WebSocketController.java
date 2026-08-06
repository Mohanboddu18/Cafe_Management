package com.cafe.management.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/order/update")
    @SendTo("/topic/orders")
    public String broadcastOrderUpdate(String message) {
        return message;
    }
}
