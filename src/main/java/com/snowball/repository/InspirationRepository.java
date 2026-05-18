package com.snowball.repository;

import com.snowball.entity.Inspiration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InspirationRepository extends JpaRepository<Inspiration, Long> {
    List<Inspiration> findByUserIdOrderByCreatedAtDesc(Long userId);
}
