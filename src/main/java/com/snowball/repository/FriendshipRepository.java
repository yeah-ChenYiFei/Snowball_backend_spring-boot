package com.snowball.repository;

import com.snowball.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' AND " +
        "((f.userId = :user1 AND f.friendId = :user2) OR (f.userId = :user2 AND f.friendId = :user1))")
    Optional<Friendship> findAcceptedFriendship(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT f FROM Friendship f WHERE " +
        "(f.userId = :user1 AND f.friendId = :user2) OR (f.userId = :user2 AND f.friendId = :user1)")
    Optional<Friendship> findAnyFriendship(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT f FROM Friendship f WHERE f.status = 'ACCEPTED' AND " +
        "(f.userId = :userId OR f.friendId = :userId)")
    List<Friendship> findAllFriends(@Param("userId") Long userId);

    List<Friendship> findByFriendIdAndStatus(Long friendId, Friendship.FriendshipStatus status);

    List<Friendship> findByUserIdAndStatus(Long userId, Friendship.FriendshipStatus status);
}
