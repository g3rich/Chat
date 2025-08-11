package ru.rsreu.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rsreu.chat.entity.MessageRead;

import java.util.List;

public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);

    List<MessageRead> findByUserIdAndMessageChatId(Long userId, Long chatId);

    long countByMessageId(Long messageId);

    List<MessageRead> findByMessageId(Long messageId);

    void deleteByMessageId(Long messageId);

    void deleteByMessageChatId(Long chatId);

    void deleteByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM MessageRead mr WHERE mr.message.id IN :messageIds")
    void deleteByMessageIds(@Param("messageIds") List<Long> messageIds);

}
