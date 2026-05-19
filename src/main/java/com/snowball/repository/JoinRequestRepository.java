package com.snowball.repository;

import com.snowball.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByWorldIdOrderByCreatedAtDesc(Long worldId);
    List<JoinRequest> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
    boolean existsByWorldIdAndApplicantIdAndStatus(Long worldId, Long applicantId, JoinRequest.JoinRequestStatus status);
}
