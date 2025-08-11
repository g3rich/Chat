package ru.rsreu.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import ru.rsreu.chat.dto.MessageDto;
import ru.rsreu.chat.dto.UserDto;
import ru.rsreu.chat.service.ChatService;
import ru.rsreu.chat.service.UserService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(
            @DestinationVariable Long chatId,
            MessageDto messageDto,
            Principal principal) {
        UserDto user;
        if (principal != null) {
            // Аутентифицированный пользователь
            user = userService.findByUsername(principal.getName());
        } else {
            // Неаутентифицированный клиент — например, виджет
            user = userService.findById(messageDto.getSender().getId());
        }

        MessageDto savedMessage = chatService.sendMessage(
                chatId,
                user.getId(),
                messageDto.getContent(),
                messageDto.getType());


        savedMessage.setAction("SEND");
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, savedMessage);
    }
}