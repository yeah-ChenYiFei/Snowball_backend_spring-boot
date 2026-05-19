package com.snowball.repository;

import com.snowball.entity.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {

    @Query("SELECT m FROM PrivateMessage m WHERE " +
        "(m.senderId = :user1 AND m.receiverId = :user2) OR " +
        "(m.senderId = :user2 AND m.receiverId = :user1) " +
        "ORDER BY m.createdAt ASC")
    List<PrivateMessage> findConversation(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT m FROM PrivateMessage m WHERE " +
        "((m.senderId = :user1 AND m.receiverId = :user2) OR " +
        "(m.senderId = :user2 AND m.receiverId = :user1)) " +
        "AND m.id > :sinceId ORDER BY m.createdAt ASC")
    List<PrivateMessage> findConversationSince(@Param("user1") Long user1,
                                               @Param("user2") Long user2,
                                               @Param("sinceId") Long sinceId);

    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END " +
        "FROM PrivateMessage m WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<Long> findChatPartnerIds(@Param("userId") Long userId);

    long countByReceiverIdAndIsReadFalse(Long receiverId);
}
