package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.WorldEntryCreateDTO;
import com.snowball.entity.World;
import com.snowball.entity.WorldChange;
import com.snowball.entity.WorldEntry;
import com.snowball.entity.User;
import com.snowball.repository.WorldChangeRepository;
import com.snowball.repository.WorldCollaboratorRepository;
import com.snowball.repository.WorldEntryRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.NotificationService;
import com.snowball.service.WorldEntryService;
import com.snowball.vo.WorldEntryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorldEntryServiceImpl implements WorldEntryService {

    private final WorldEntryRepository entryRepository;
    private final WorldRepository worldRepository;
    private final WorldCollaboratorRepository collaboratorRepository;
    private final WorldChangeRepository changeRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public WorldEntryServiceImpl(WorldEntryRepository entryRepository,
                                  WorldRepository worldRepository,
                                  WorldCollaboratorRepository collaboratorRepository,
                                  WorldChangeRepository changeRepository,
                                  NotificationService notificationService,
                                  UserRepository userRepository) {
        this.entryRepository = entryRepository;
        this.worldRepository = worldRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.changeRepository = changeRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
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
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        // Owner creates directly; collaborator creates pending change
        if (world.getUserId().equals(userId)) {
            return createDirect(worldId, userId, dto);
        }

        if (collaboratorRepository.existsByWorldIdAndUserId(worldId, userId)) {
            return createPendingChange(worldId, userId, null, dto.getName(), dto.getType(), dto.getContent(), "CREATE", world);
        }

        throw new BusinessException(403, "无权在此世界创建设定");
    }

    @Override
    public WorldEntryVO updateEntry(Long entryId, Long userId, WorldEntryCreateDTO dto) {
        WorldEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException(404, "设定条目不存在"));
        Long worldId = entry.getWorldId();
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        // Owner edits directly; collaborator creates pending change
        if (world.getUserId().equals(userId)) {
            entry.setName(dto.getName());
            entry.setType(dto.getType());
            entry.setContent(dto.getContent());
            return toVO(entryRepository.save(entry));
        }

        if (collaboratorRepository.existsByWorldIdAndUserId(worldId, userId)) {
            return createPendingChange(worldId, userId, entryId, dto.getName(), dto.getType(), dto.getContent(), "UPDATE", world);
        }

        throw new BusinessException(403, "只能编辑自己的设定条目");
    }

    @Override
    public void deleteEntry(Long entryId, Long userId) {
        WorldEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException(404, "设定条目不存在"));
        Long worldId = entry.getWorldId();
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        // Owner deletes directly; collaborator creates pending change
        if (world.getUserId().equals(userId)) {
            entryRepository.delete(entry);
            return;
        }

        if (collaboratorRepository.existsByWorldIdAndUserId(worldId, userId)) {
            createPendingChange(worldId, userId, entryId, entry.getName(), entry.getType(), entry.getContent(), "DELETE", world);
            return;
        }

        throw new BusinessException(403, "只能删除自己的设定条目");
    }

    private WorldEntryVO createDirect(Long worldId, Long userId, WorldEntryCreateDTO dto) {
        WorldEntry entry = new WorldEntry();
        entry.setWorldId(worldId);
        entry.setUserId(userId);
        entry.setName(dto.getName());
        entry.setType(dto.getType());
        entry.setContent(dto.getContent());
        return toVO(entryRepository.save(entry));
    }

    private WorldEntryVO createPendingChange(Long worldId, Long userId, Long entryId,
                                              String name, String type, String content,
                                              String changeType, World world) {
        WorldChange change = new WorldChange();
        change.setWorldId(worldId);
        change.setUserId(userId);
        change.setEntryId(entryId);
        change.setEntryName(name);
        change.setEntryType(type);
        change.setEntryContent(content);
        change.setChangeType(changeType);
        change.setStatus("PENDING");
        changeRepository.save(change);

        String actorName = userRepository.findById(userId).map(User::getUsername).orElse("");
        String actionLabel = switch (changeType) {
            case "CREATE" -> "新增了一些设定";
            case "UPDATE" -> "修改了一些设定";
            case "DELETE" -> "删除了一些设定";
            default -> "编辑了一些设定";
        };
        notificationService.create(world.getUserId(), "WORLD_COLLABORATOR_CHANGE", worldId, "WORLD",
                userId, actorName + "为您的世界「" + world.getName() + "」" + actionLabel);

        // Return a placeholder VO indicating pending
        WorldEntryVO vo = new WorldEntryVO();
        vo.setWorldId(worldId);
        vo.setUserId(userId);
        vo.setName(name);
        vo.setType(type);
        vo.setContent(content);
        vo.setContentPreview("【待审批】" + (content != null && content.length() > 60 ? content.substring(0, 60) + "..." : content));
        return vo;
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
