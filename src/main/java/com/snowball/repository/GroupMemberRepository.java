package com.snowball.repository;
import com.snowball.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByUserId(Long userId);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
    long countByGroupId(Long groupId);

    @Query("SELECT gm.groupId, COUNT(gm) FROM GroupMember gm WHERE gm.groupId IN :groupIds GROUP BY gm.groupId")
    List<Object[]> countByGroupIdIn(@Param("groupIds") List<Long> groupIds);
}
