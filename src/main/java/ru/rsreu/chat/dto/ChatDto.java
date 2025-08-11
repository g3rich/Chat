package ru.rsreu.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private Long id;
    private String name;
    private boolean isGroup;
    private List<UserDto> participants;
    private MessageDto lastMessage;
    private boolean hasUnreadMessages;
    private long unreadCount;
}