package ru.rsreu.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long chatId;
    private UserDto sender;
    private String content;
    private String type;
    private LocalDateTime sentAt;
    private String action; // "SEND", "DELETE", "EDIT" - для WebSocket
    private long readCount;
}