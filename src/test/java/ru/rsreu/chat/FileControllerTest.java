package ru.rsreu.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TempDir
    Path tempUploadDir;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testUploadFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test file content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                        .file(file)
                        .param("chatId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").exists())
                .andExpect(jsonPath("$.fileName").value("test.txt"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testUploadEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                        .file(emptyFile)
                        .param("chatId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Файл пустой"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testUploadWithoutFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                        .param("chatId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testUploadWithoutChatId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test file content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testUploadLargeFile() throws Exception {
        byte[] largeFileContent = new byte[1024 * 1024 * 11];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.bin",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                largeFileContent
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/upload")
                        .file(largeFile)
                        .param("chatId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is2xxSuccessful());
    }
}