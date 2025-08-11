package ru.rsreu.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.chat.dto.*;
import ru.rsreu.chat.service.ChatService;
import ru.rsreu.chat.service.UserService;

@RestController
@RequestMapping("/api/widget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WidgetController {
    private final UserService userService;
    private final ChatService chatService;

    @PostMapping("/start")
    public ResponseEntity<WidgetStartResponse> startChat(@RequestParam String name) {
        try {
            UserDto client = userService.createClientUser(name + "(Клиент " + userService.findClientNextNumber() + ")");
            UserDto consultant = userService.findByUsername("user1");

            ChatDto chat = chatService.createPrivateChat(
                    client.getId(),
                    consultant.getId(),
                    "Чат с " + client.getName()
            );

            WidgetStartResponse response = new WidgetStartResponse();
            response.setClient(client);
            response.setConsultant(consultant);
            response.setChat(chat);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}