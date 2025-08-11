package ru.rsreu.chat.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rsreu.chat.entity.Message;
import ru.rsreu.chat.entity.MessageRead;
import ru.rsreu.chat.entity.User;
import ru.rsreu.chat.repository.MessageReadRepository;
import ru.rsreu.chat.repository.MessageRepository;
import ru.rsreu.chat.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository; // если нужно получать пользователя

    @Transactional
    public void markMessagesAsRead(Long chatId, Long userId) {
        List<Message> messages = messageRepository.findAllByChatIdOrderBySentAtAsc(chatId);

        for (Message message : messages) {
            if (!message.getSender().getId().equals(userId)) {
                boolean alreadyRead = messageReadRepository.existsByMessageIdAndUserId(message.getId(), userId);
                if (!alreadyRead) {
                    MessageRead read = new MessageRead();
                    read.setMessage(message);
                    read.setUser(userRepository.findById(userId).orElseThrow()); // либо userRepository.findById(userId).orElseThrow()
                    read.setReadAt(LocalDateTime.now());
                    messageReadRepository.save(read);
                }
            }
        }
    }

    @Transactional
    public void deleteMessageById(Long messageId) {
        messageReadRepository.deleteByMessageId(messageId);
        messageRepository.deleteById(messageId);
    }
}
