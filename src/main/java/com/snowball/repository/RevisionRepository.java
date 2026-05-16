package com.snowball.repository;
import com.snowball.entity.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RevisionRepository extends JpaRepository<Revision, Long> {
    List<Revision> findByOriginalPostIdOrderByCreatedAtDesc(Long originalPostId);
}
