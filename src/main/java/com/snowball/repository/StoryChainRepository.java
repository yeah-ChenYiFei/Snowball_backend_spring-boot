package com.snowball.repository;
import com.snowball.entity.StoryChain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoryChainRepository extends JpaRepository<StoryChain, Long> {
    List<StoryChain> findByGroupIdOrderByCreatedAtDesc(Long groupId);
    List<StoryChain> findByGroupIdIsNullOrderByCreatedAtDesc();
}
