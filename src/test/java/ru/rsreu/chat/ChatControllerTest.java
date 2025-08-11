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
import ru.rsreu.chat.dto.*;
import ru.rsreu.chat.service.ChatService;
import ru.rsreu.chat.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "user1")
    public void testGetUserChats() throws Exception {
        UserDto user = new UserDto(1L, "user1", "User One", null);
        ChatDto chat1 = new ChatDto(1L, "Chat 1", false, null, null, false, 0);
        ChatDto chat2 = new ChatDto(2L, "Chat 2", true, null, null, true, 3);

        Mockito.when(userService.findByUsername("user1")).thenReturn(user);
        Mockito.when(chatService.getUserChats(1L)).thenReturn(Arrays.asList(chat1, chat2));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].hasUnreadMessages").value(true));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testCreatePrivateChat() throws Exception {
        UserDto currentUser = new UserDto(1L, "user1", "User One", null);
        ChatDto chat = new ChatDto(3L, "Private Chat", false, null, null, false, 0);

        Mockito.when(userService.findByUsername("user1")).thenReturn(currentUser);
        Mockito.when(chatService.createPrivateChat(1L, 2L, null)).thenReturn(chat);

        mockMvc.perform(post("/api/chats/private/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L));
    }

    @Test
    @WithMockUser(username = "user1")
    public void testGetChatMessages() throws Exception {
        UserDto user = new UserDto(1L, "user1", "User One", null);
        MessageDto message1 = new MessageDto(1L, 1L, null, "Hello", "text", null, null, 0);
        MessageDto message2 = new MessageDto(2L, 1L, null, "Hi", "text", null, null, 0);

        Mockito.when(userService.findByUsername("user1")).thenReturn(user);
        Mockito.when(chatService.getChatMessages(1L, 1L)).thenReturn(Arrays.asList(message1, message2));

        mockMvc.perform(get("/api/chats/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello"))
                .andExpect(jsonPath("$[1].content").value("Hi"));
    }
}