package ru.rsreu.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.chat.entity.User;
import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByNameContainingIgnoreCase(String name);

    long countByUsernameNotNull();

    List<User> findByUsernameContainingOrNameContaining(String usernameQuery, String nameQuery);

    List<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String username, String name);

    Optional<User> findTopByUsernameStartingWithOrderByIdDesc(String prefix);
}