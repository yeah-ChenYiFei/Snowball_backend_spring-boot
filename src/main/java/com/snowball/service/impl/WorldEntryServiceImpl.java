package com.snowball.service.impl;

import com.snowball.dto.WorldEntryCreateDTO;
import com.snowball.entity.WorldEntry;
import com.snowball.repository.WorldEntryRepository;
import com.snowball.service.WorldEntryService;
import com.snowball.vo.WorldEntryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorldEntryServiceImpl implements WorldEntryService {

    private final WorldEntryRepository entryRepository;

    public WorldEntryServiceImpl(WorldEntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Override
    public List<WorldEntryVO> getEntries(Long worldId, String type, String search) {
        List<WorldEntry> entries;

        boolean hasType = StringUtils.hasText(type);
        boolean hasSearch = StringUtils.hasText(search);

        if (hasType && hasSearch) {
            entries = entryRepository.searchByWorldIdAndTypeAndName(worldId, type, search);
        } else if (hasType) {
            entries = entryRepository.findByWorldIdAndTypeOrderByCreatedAtDesc(worldId, type);
        } else if (hasSearch) {
            entries = entryRepository.searchByWorldIdAndName(worldId, search);
        } else {
            entries = entryRepository.findByWorldIdOrderByCreatedAtDesc(worldId);
        }

        return entries.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<String> getEntryTypes(Long worldId) {
        return entryRepository.findDistinctTypesByWorldId(worldId);
    }

    @Override
    public WorldEntryVO getEntry(Long entryId) {
        WorldEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("设定条目不存在"));
        return toVO(entry);
    }

    @Override
    public WorldEntryVO createEntry(Long worldId, Long userId, WorldEntryCreateDTO dto) {
        WorldEntry entry = new WorldEntry();
        entry.setWorldId(worldId);
        entry.setUserId(userId);
        entry.setName(dto.getName());
        entry.setType(dto.getType());
        entry.setContent(dto.getContent());
        return toVO(entryRepository.save(entry));
    }

    @Override
    public WorldEntryVO updateEntry(Long entryId, Long userId, WorldEntryCreateDTO dto) {
        WorldEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("设定条目不存在"));
        entry.setName(dto.getName());
        entry.setType(dto.getType());
        entry.setContent(dto.getContent());
        return toVO(entryRepository.save(entry));
    }

    @Override
    public void deleteEntry(Long entryId) {
        entryRepository.deleteById(entryId);
    }

    private WorldEntryVO toVO(WorldEntry e) {
        WorldEntryVO vo = new WorldEntryVO();
        vo.setId(e.getId());
        vo.setWorldId(e.getWorldId());
        vo.setUserId(e.getUserId());
        vo.setName(e.getName());
        vo.setType(e.getType());
        vo.setContent(e.getContent());
        // 截取前80字作为预览
        String content = e.getContent();
        if (content != null && content.length() > 80) {
            vo.setContentPreview(content.substring(0, 80) + "...");
        } else {
            vo.setContentPreview(content);
        }
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
