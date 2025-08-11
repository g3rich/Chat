package ru.rsreu.chat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rsreu.chat.dto.ChatDto;
import ru.rsreu.chat.dto.UserDto;
import ru.rsreu.chat.dto.WidgetStartResponse;
import ru.rsreu.chat.service.ChatService;
import ru.rsreu.chat.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class WidgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ChatService chatService;

    @Test
    public void testStartChat() throws Exception {
        UserDto client = new UserDto(1L, "client1", "Client One", null);
        UserDto consultant = new UserDto(2L, "consultant1", "Consultant One", null);
        ChatDto chat = new ChatDto(1L, "Chat with Client One", false, null, null, false, 0);
        WidgetStartResponse response = new WidgetStartResponse(client, consultant, chat);

        Mockito.when(userService.createClientUser(Mockito.anyString())).thenReturn(client);
        Mockito.when(userService.findByUsername("user1")).thenReturn(consultant);
        Mockito.when(chatService.createPrivateChat(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(chat);

        mockMvc.perform(post("/api/widget/start")
                        .param("name", "Client One"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.name").value("Client One"))
                .andExpect(jsonPath("$.chat.id").value(1L));
    }
}