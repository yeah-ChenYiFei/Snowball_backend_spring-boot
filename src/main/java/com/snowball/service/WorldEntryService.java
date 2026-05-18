package com.snowball.service;

import com.snowball.dto.WorldEntryCreateDTO;
import com.snowball.vo.WorldEntryVO;

import java.util.List;

public interface WorldEntryService {
    List<WorldEntryVO> getEntries(Long worldId, String type, String search);
    WorldEntryVO getEntry(Long entryId);
    List<String> getEntryTypes(Long worldId);
    WorldEntryVO createEntry(Long worldId, Long userId, WorldEntryCreateDTO dto);
    WorldEntryVO updateEntry(Long entryId, Long userId, WorldEntryCreateDTO dto);
    void deleteEntry(Long entryId, Long userId);
}
