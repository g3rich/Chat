package ru.rsreu.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetStartResponse {
    private UserDto client;
    private UserDto consultant;
    private ChatDto chat;
}