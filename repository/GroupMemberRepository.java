package com.example.snowball.repository;
import com.example.snowball.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
}
