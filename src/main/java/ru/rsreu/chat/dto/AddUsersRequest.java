package ru.rsreu.chat.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddUsersRequest {
    private List<Long> userIds;
}
