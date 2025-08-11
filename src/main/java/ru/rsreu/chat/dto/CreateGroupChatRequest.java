package ru.rsreu.chat.dto;

import lombok.Data;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupChatRequest {
    private String chatName;
    private List<Long> participantIds;
}
