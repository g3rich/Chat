package ru.rsreu.chat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.rsreu.chat.dto.ChatDto;
import ru.rsreu.chat.dto.UserDto;
import ru.rsreu.chat.service.ChatService;
import ru.rsreu.chat.service.MessageService;
import ru.rsreu.chat.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageService messageService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllUsers() throws Exception {
        UserDto user1 = new UserDto(1L, "user1", "User One", null);
        UserDto user2 = new UserDto(2L, "user2", "User Two", null);

        Mockito.when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteUser() throws Exception {
        UserDto user1 = new UserDto(1L, "user1", "User One", null);
        mockMvc.perform(delete("/api/admin/users/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetAllChats() throws Exception {
        ChatDto chat1 = new ChatDto(1L, "Chat 1", false, null, null, false, 0);
        ChatDto chat2 = new ChatDto(2L, "Chat 2", true, null, null, false, 0);

        Mockito.when(chatService.getAllChats()).thenReturn(Arrays.asList(chat1, chat2));

        mockMvc.perform(get("/api/admin/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].group").value(false))
                .andExpect(jsonPath("$[1].group").value(true));
    }
}
