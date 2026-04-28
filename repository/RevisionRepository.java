package com.example.snowball.repository;
import com.example.snowball.entity.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RevisionRepository extends JpaRepository<Revision, Long> {
    List<Revision> findByOriginalPostIdOrderByCreatedAtDesc(Long originalPostId);
}
