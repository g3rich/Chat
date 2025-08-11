package ru.rsreu.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.chat.entity.Message;

import java.util.List;
import java.util.Optional;

// repository/MessageRepository.java
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByChatIdOrderBySentAtAsc(Long chatId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.sentAt DESC LIMIT 1")
    Optional<Message> findLastMessageByChatId(@Param("chatId") Long chatId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId " +
            "AND m.sender.id != :userId " +
            "AND NOT EXISTS (SELECT mr FROM MessageRead mr WHERE mr.message = m AND mr.user.id = :userId)")
    long countByChatIdAndSenderIdNotAndNotReadByUser(@Param("chatId") Long chatId,
                                                     @Param("userId") Long userId);
    void deleteByChatId(Long chatId);
    void deleteBySenderId(Long userId);

    @Modifying
    @Query("SELECT m.id FROM Message m WHERE m.sender.id = :userId")
    List<Long> findIdsBySenderId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender.id = :userId")
    void deleteAllBySenderId(@Param("userId") Long userId);
}