package ru.rsreu.chat.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.rsreu.chat.dto.RegisterRequest;
import ru.rsreu.chat.dto.UserDto;
import ru.rsreu.chat.entity.Chat;
import ru.rsreu.chat.entity.Role;
import ru.rsreu.chat.entity.User;
import ru.rsreu.chat.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final MessageRepository messageRepository;
    private final UserChatRepository userChatRepository;
    private final MessageReadRepository messageReadRepository;
    private final ChatRepository chatRepository;

    // Для регистрации через RegisterRequest
    public UserDto register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setName(registerRequest.getName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Хеширование

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(userRole));


        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    public UserDto findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> modelMapper.map(user, UserDto.class))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    public UserDto findById(Long id) {
       return userRepository.findById(id)
               .map(user -> modelMapper.map(user, UserDto.class))
               .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public List<UserDto> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(query, query)
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    public UserDto createClientUser(String name) {
        // Находим последнего клиента
        int nextNumber = findClientNextNumber();

        User user = new User();
        user.setUsername("client" + nextNumber);
        user.setName(name);
        user.setPassword(passwordEncoder.encode("temp_password")); // Временный пароль

        Role userRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(Set.of(userRole));


        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    public int findClientNextNumber() {
        Optional<User> lastClient = userRepository.findTopByUsernameStartingWithOrderByIdDesc("client");

        int nextNumber = 1;
        if (lastClient.isPresent()) {
            String lastUsername = lastClient.get().getUsername();
            nextNumber = Integer.parseInt(lastUsername.replace("client", "")) + 1;
        }
        return nextNumber;
    }

    @Transactional
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 1. Найти все чаты пользователя ДО удаления связей
        List<Chat> userChats = chatRepository.findAllByUserId(userId);

        // 2. Удалить все прочтения сообщений, отправленных пользователем
        List<Long> userMessageIds = messageRepository.findIdsBySenderId(userId);
        if (!userMessageIds.isEmpty()) {
            messageReadRepository.deleteByMessageIds(userMessageIds);
        }

        // 3. Удалить прочтения пользователя
        messageReadRepository.deleteByUserId(userId);

        // 4. Удалить сообщения пользователя
        messageRepository.deleteAllBySenderId(userId);

        // 5. Удалить связи с чатами
        userChatRepository.deleteByUserId(userId);

        // 6. Удалить чаты, которые после этого стали пустыми
        for (Chat chat : userChats) {
            Long chatId = chat.getId();
            if (userChatRepository.findAllByChatId(chatId).isEmpty()) {
                messageReadRepository.deleteByMessageChatId(chatId);
                messageRepository.deleteByChatId(chatId);
                userChatRepository.deleteByChatId(chatId);
                chatRepository.deleteById(chatId);
            }
        }

        // 7. Удалить пользователя
        userRepository.delete(user);
    }
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

}