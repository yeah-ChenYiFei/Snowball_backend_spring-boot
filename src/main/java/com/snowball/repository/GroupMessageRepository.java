package com.snowball.repository;
import com.snowball.entity.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupIdOrderByCreatedAtAsc(Long groupId);
    List<GroupMessage> findByGroupIdAndIdGreaterThanOrderByCreatedAtAsc(Long groupId, Long sinceId);
}
