package com.snowball.repository;
import com.snowball.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByNameContainingIgnoreCase(String name);
    List<Group> findByNameContainingIgnoreCaseAndIsSearchableTrue(String name);

    @Query("SELECT g FROM Group g JOIN GroupMember gm ON g.id = gm.groupId WHERE gm.userId = :userId")
    List<Group> findByMemberUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(MAX(g.groupNumber), 0) FROM Group g")
    java.util.Optional<Long> findMaxGroupNumber();
}
