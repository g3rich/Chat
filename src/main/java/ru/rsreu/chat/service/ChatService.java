package ru.rsreu.chat.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.rsreu.chat.dto.*;
import ru.rsreu.chat.entity.*;
import ru.rsreu.chat.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final ModelMapper modelMapper;
    private final UserChatRepository userChatRepository;

    public List<ChatDto> getUserChats(Long userId) {
        List<Chat> chats = chatRepository.findAllByUserId(userId);

        List<ChatDto> chatDtos = chats.stream().map(chat -> {
            ChatDto chatDto = modelMapper.map(chat, ChatDto.class);

            // Участники
            chatDto.setParticipants(chat.getUserChats().stream()
                    .map(uc -> modelMapper.map(uc.getUser(), UserDto.class))
                    .collect(Collectors.toList()));

            // Последнее сообщение
            Optional<Message> lastMsgOpt = messageRepository.findLastMessageByChatId(chat.getId());
            lastMsgOpt.ifPresent(message -> {
                chatDto.setLastMessage(modelMapper.map(message, MessageDto.class));
            });

            // Количество непрочитанных сообщений
            if (lastMsgOpt.isPresent()) {
                Message lastMessage = lastMsgOpt.get();
                boolean isUnread = !lastMessage.getSender().getId().equals(userId) &&
                        !messageReadRepository.existsByMessageIdAndUserId(lastMessage.getId(), userId);
                chatDto.setHasUnreadMessages(isUnread);

                // Подсчет непрочитанных сообщений
                long unreadCount = messageRepository.countByChatIdAndSenderIdNotAndNotReadByUser(chat.getId(), userId);
                chatDto.setUnreadCount(unreadCount);
            }

            return chatDto;
        }).collect(Collectors.toList());

        // Сортировка
        chatDtos.sort((c1, c2) -> {
            LocalDateTime time1 = c1.getLastMessage() != null ?
                    c1.getLastMessage().getSentAt() :
                    LocalDateTime.MIN;
            LocalDateTime time2 = c2.getLastMessage() != null ?
                    c2.getLastMessage().getSentAt() :
                    LocalDateTime.MIN;
            return time2.compareTo(time1);
        });

        return chatDtos;
    }

    public List<ChatDto> getAllChats() {
        return chatRepository.findAllChats().stream()
                .map(chat -> {
                    ChatDto dto = modelMapper.map(chat, ChatDto.class);
                    // Явно заполняем участников, так как их нет напрямую в сущности Chat
                    dto.setParticipants(
                            chat.getUserChats().stream()
                                    .map(UserChat::getUser)
                                    .map(user -> modelMapper.map(user, UserDto.class))
                                    .toList()
                    );
                    return dto;
                })
                .toList();
    }

    public ChatDto createPrivateChat(Long user1Id, Long user2Id, String customName) {
        // Проверка, существует ли уже приватный чат между user1 и user2
        Optional<Chat> existingChat = chatRepository.findPrivateChatBetweenUsers(user1Id, user2Id);
        if (existingChat.isPresent()) {
            Chat existing = existingChat.get();
            ChatDto existingDto = modelMapper.map(existing, ChatDto.class);
            existingDto.setParticipants(existing.getUserChats().stream()
                    .map(uc -> modelMapper.map(uc.getUser(), UserDto.class))
                    .collect(Collectors.toList()));
            messageRepository.findLastMessageByChatId(existing.getId())
                    .ifPresent(message -> existingDto.setLastMessage(modelMapper.map(message, MessageDto.class)));
            return existingDto;
        }

        // Если нет — создать чат
        User user1 = userRepository.findById(user1Id).orElseThrow();
        User user2 = userRepository.findById(user2Id).orElseThrow();

        Chat chat = new Chat();
        chat.setName(customName != null ? customName : user1.getName() + " & " + user2.getName());
        chat.setGroup(false);

        UserChat userChat1 = new UserChat(null, user1, chat, LocalDateTime.now());
        UserChat userChat2 = new UserChat(null, user2, chat, LocalDateTime.now());

        chat.getUserChats().add(userChat1);
        chat.getUserChats().add(userChat2);

        chat = chatRepository.save(chat);

        ChatDto chatDto = modelMapper.map(chat, ChatDto.class);
        chatDto.setParticipants(List.of(
                modelMapper.map(user1, UserDto.class),
                modelMapper.map(user2, UserDto.class)
        ));
        return chatDto;
    }

    public ChatDto createGroupChat(Long creatorId, CreateGroupChatRequest request) {
        Chat chat = new Chat();
        chat.setName(request.getChatName());
        chat.setGroup(true);
        chat = chatRepository.save(chat);
        System.out.println("Chat name: " + chat.getName());

        List<User> participants = userRepository.findAllById(request.getParticipantIds());

        // Добавляем создателя тоже, если его нет в списке
        if (participants.stream().noneMatch(u -> u.getId().equals(creatorId))) {
            userRepository.findById(creatorId).ifPresent(participants::add);
        }

        for (User user : participants) {
            chat.getUserChats().add(new UserChat(null, user, chat, LocalDateTime.now()));
        }

        return modelMapper.map(chatRepository.save(chat), ChatDto.class);
    }

    public List<MessageDto> getChatMessages(Long chatId, Long currentUserId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        boolean isGroup = chat.isGroup();

        return messageRepository.findAllByChatIdOrderBySentAtAsc(chatId).stream()
                .map(message -> {
                    MessageDto dto = modelMapper.map(message, MessageDto.class);
                    dto.setReadCount(messageReadRepository.countByMessageId(message.getId()));

                    // Определим, прочитано ли сообщение по условиям задачи:
                    boolean isRead;
                    if (isGroup) {
                        // Прочитано, если кто-то кроме отправителя его прочёл
                        isRead = messageReadRepository
                                .findByMessageId(message.getId()).stream()
                                .anyMatch(read -> !read.getUser().getId().equals(message.getSender().getId()));
                    } else {
                        // Прочитано, если собеседник (не отправитель) прочёл
                        isRead = messageReadRepository
                                .existsByMessageIdAndUserId(message.getId(),
                                        !message.getSender().getId().equals(currentUserId) ? currentUserId : null);
                    }

                    dto.setAction(isRead ? "READ" : "UNREAD"); // Или можно завести отдельное поле
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public MessageDto sendMessage(Long chatId, Long senderId, String content, String type) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        User sender = userRepository.findById(senderId).orElseThrow();

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setType(type);
        message.setSentAt(LocalDateTime.now());

        return modelMapper.map(messageRepository.save(message), MessageDto.class);
    }

    public ChatDto getChatById(Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        ChatDto chatDto = modelMapper.map(chat, ChatDto.class);

        // Участники чата
        chatDto.setParticipants(chat.getUserChats().stream()
                .map(userChat -> modelMapper.map(userChat.getUser(), UserDto.class))
                .collect(Collectors.toList()));

        // Последнее сообщение (если есть)
        messageRepository.findLastMessageByChatId(chatId)
                .ifPresent(message -> chatDto.setLastMessage(modelMapper.map(message, MessageDto.class)));

        return chatDto;
    }

    @Transactional
    public void markMessagesAsRead(Long chatId, Long userId) {
        List<Message> messages = messageRepository.findAllByChatIdOrderBySentAtAsc(chatId);

        List<Message> unreadMessages = messages.stream()
                .filter(message -> !message.getSender().getId().equals(userId))
                .filter(message -> !messageReadRepository.existsByMessageIdAndUserId(message.getId(), userId))
                .toList();

        User user = userRepository.findById(userId).orElseThrow();

        for (Message message : unreadMessages) {
            MessageRead read = new MessageRead();
            read.setMessage(message);
            read.setUser(user);
            read.setReadAt(LocalDateTime.now());
            messageReadRepository.save(read);
        }
    }

    public void addUsersToChat(Long chatId, List<Long> userIds) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        if (!chat.isGroup()) {
            throw new IllegalArgumentException("Cannot add users to a private chat.");
        }

        for (Long userId : userIds) {
            User user = userRepository.findById(userId).orElseThrow();
            if (chat.getUserChats().stream().noneMatch(uc -> uc.getUser().getId().equals(userId))) {
                UserChat userChat = new UserChat();
                userChat.setUser(user);
                userChat.setChat(chat);
                userChat.setJoinedAt(LocalDateTime.now());
                userChatRepository.save(userChat);
            }
        }
    }

    @Transactional
    public void deleteChatById(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        // Удаляем связанные сообщения
        messageReadRepository.deleteByMessageChatId(chatId); // если есть таблица message_read
        messageRepository.deleteByChatId(chatId);

        // Удаляем связи пользователей и чатов
        userChatRepository.deleteByChatId(chatId);

        // Удаляем сам чат
        chatRepository.delete(chat);
    }

}