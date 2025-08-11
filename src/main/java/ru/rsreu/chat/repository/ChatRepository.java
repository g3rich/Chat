package ru.rsreu.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.chat.entity.Chat;

import java.util.List;
import java.util.Optional;

// repository/ChatRepository.java
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c JOIN c.userChats uc WHERE uc.user.id = :userId")
    List<Chat> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Chat c JOIN c.userChats uc1 JOIN c.userChats uc2 " +
            "WHERE c.isGroup = false AND uc1.user.id = :user1Id AND uc2.user.id = :user2Id")
    Optional<Chat> findPrivateChatBetweenUsers(@Param("user1Id") Long user1Id,
                                               @Param("user2Id") Long user2Id);
    @Modifying
    @Query("DELETE FROM Chat c WHERE c.userChats IS EMPTY")
    void deleteChatsWithoutUsers();

    @Query("""
    SELECT DISTINCT c FROM Chat c
    LEFT JOIN FETCH c.userChats uc
    LEFT JOIN FETCH uc.user
    """)
    List<Chat> findAllChats();
}