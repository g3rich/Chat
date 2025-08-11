package ru.rsreu.chat.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ru.rsreu.chat.dto.*;
import ru.rsreu.chat.entity.Chat;
import ru.rsreu.chat.service.ChatService;
import ru.rsreu.chat.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ChatDto>> getUserChats(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getUserChats(user.getId()));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageDto>> getChatMessages(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(chatService.getChatMessages(chatId, user.getId()));
    }


    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long chatId,
            @RequestBody String content,
            @RequestBody String type,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(chatService.sendMessage(chatId, user.getId(), content, type));
    }

    @PostMapping("/private/{userId}")
    public ResponseEntity<ChatDto> createPrivateChat(
            @PathVariable Long userId,
            @RequestBody(required = false) CreateChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current Authentication in SecurityContext: " + auth);
        UserDto currentUser = userService.findByUsername(userDetails.getUsername());
        String chatName = request != null ? request.getChatName() : null;
        return ResponseEntity.ok(chatService.createPrivateChat(currentUser.getId(), userId, chatName));
    }

    @PostMapping("/group")
    public ResponseEntity<ChatDto> createGroupChat(
            @RequestBody(required = false) CreateGroupChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto creator = userService.findByUsername(userDetails.getUsername());
        System.out.println("Chat name1: " + request.getChatName());
        System.out.println("Ids: " + request.getParticipantIds());
        return ResponseEntity.ok(chatService.createGroupChat(creator.getId(), request));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDto> getChatInfo(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto currentUser = userService.findByUsername(userDetails.getUsername());
        ChatDto chat = chatService.getChatById(chatId);

        // Если чат не групповой — задать имя собеседника
        if (!chat.isGroup() && chat.getParticipants() != null) {
            chat.setName(
                    chat.getParticipants()
                            .stream()
                            .filter(p -> !p.getId().equals(currentUser.getId()))
                            .map(UserDto::getName)
                            .findFirst()
                            .orElse("Без имени")
            );
        }

        return ResponseEntity.ok(chat);
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto user = userService.findByUsername(userDetails.getUsername());
        chatService.markMessagesAsRead(chatId, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUsers(query));
    }

    @PostMapping("/{chatId}/add-users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addUsersToChat(@PathVariable Long chatId, @RequestBody AddUsersRequest request) {
        chatService.addUsersToChat(chatId, request.getUserIds());
        return ResponseEntity.ok().build();
    }

}