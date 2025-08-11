package ru.rsreu.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rsreu.chat.entity.UserChat;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChatRepository extends JpaRepository<UserChat, Long> {

    Optional<UserChat> findByUserIdAndChatId(Long userId, Long chatId);

    List<UserChat> findAllByChatId(Long chatId);

    List<UserChat> findAllByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserChat uc WHERE uc.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserChat uc WHERE uc.chat.id = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);

    Object findByUserId(Long id);
}
