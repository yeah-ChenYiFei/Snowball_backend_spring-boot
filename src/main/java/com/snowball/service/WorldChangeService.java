package com.snowball.service;

import com.snowball.vo.WorldChangeVO;

import java.util.List;

public interface WorldChangeService {
    List<WorldChangeVO> getPendingChanges(Long worldId, Long ownerId);
    WorldChangeVO approveChange(Long changeId, Long worldId, Long ownerId);
    WorldChangeVO rejectChange(Long changeId, Long worldId, Long ownerId, String rejectReason);
}
