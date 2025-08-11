package ru.rsreu.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_chats")
@NoArgsConstructor
@AllArgsConstructor
public class UserChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Chat chat;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();
}