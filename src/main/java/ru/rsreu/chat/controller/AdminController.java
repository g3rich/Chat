package ru.rsreu.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.rsreu.chat.dto.ChatDto;
import ru.rsreu.chat.dto.UserDto;
import ru.rsreu.chat.service.ChatService;
import ru.rsreu.chat.service.MessageService;
import ru.rsreu.chat.service.UserService;

import java.util.List;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final ChatService chatService;
    private final UserService userService;
    private final MessageService messageService;

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId) {
        chatService.deleteChatById(chatId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/chats")
    public List<ChatDto> getAllChats() {
        System.out.println(chatService.getAllChats());
        return chatService.getAllChats();
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessageById(messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }
}
