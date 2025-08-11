package ru.rsreu.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsreu.chat.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
