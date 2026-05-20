package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.entity.User;
import com.snowball.entity.World;
import com.snowball.entity.WorldChange;
import com.snowball.entity.WorldEntry;
import com.snowball.repository.UserRepository;
import com.snowball.repository.WorldChangeRepository;
import com.snowball.repository.WorldEntryRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.service.NotificationService;
import com.snowball.service.WorldChangeService;
import com.snowball.vo.WorldChangeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorldChangeServiceImpl implements WorldChangeService {

    private final WorldChangeRepository changeRepository;
    private final WorldRepository worldRepository;
    private final WorldEntryRepository entryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public WorldChangeServiceImpl(WorldChangeRepository changeRepository,
                                   WorldRepository worldRepository,
                                   WorldEntryRepository entryRepository,
                                   UserRepository userRepository,
                                   NotificationService notificationService) {
        this.changeRepository = changeRepository;
        this.worldRepository = worldRepository;
        this.entryRepository = entryRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<WorldChangeVO> getPendingChanges(Long worldId, Long ownerId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "只有世界主人可以查看待审批修改");
        }

        List<WorldChange> changes = changeRepository.findByWorldIdAndStatusOrderByCreatedAtDesc(worldId, WorldChange.ChangeStatus.PENDING);
        return toVOList(changes);
    }

    @Override
    @Transactional
    public WorldChangeVO approveChange(Long changeId, Long worldId, Long ownerId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "只有世界主人可以审批");
        }

        WorldChange change = changeRepository.findByIdAndWorldId(changeId, worldId)
                .orElseThrow(() -> new BusinessException(404, "修改记录不存在"));
        if (change.getStatus() != WorldChange.ChangeStatus.PENDING) {
            throw new BusinessException(400, "该修改已处理");
        }

        // Execute the actual change
        switch (change.getChangeType()) {
            case CREATE -> {
                WorldEntry entry = new WorldEntry();
                entry.setWorldId(worldId);
                entry.setUserId(change.getUserId());
                entry.setName(change.getEntryName());
                entry.setType(change.getEntryType());
                entry.setContent(change.getEntryContent());
                entryRepository.save(entry);
            }
            case UPDATE -> {
                if (change.getEntryId() != null) {
                    WorldEntry entry = entryRepository.findById(change.getEntryId()).orElse(null);
                    if (entry != null && entry.getWorldId().equals(worldId)) {
                        entry.setName(change.getEntryName());
                        entry.setType(change.getEntryType());
                        entry.setContent(change.getEntryContent());
                        entryRepository.save(entry);
                    }
                }
            }
            case DELETE -> {
                if (change.getEntryId() != null) {
                    entryRepository.findById(change.getEntryId()).ifPresent(entry -> {
                        if (entry.getWorldId().equals(worldId)) {
                            entryRepository.delete(entry);
                        }
                    });
                }
            }
        }

        change.setStatus(WorldChange.ChangeStatus.APPROVED);
        change.setReviewedBy(ownerId);
        change.setReviewedAt(java.time.LocalDateTime.now());
        changeRepository.save(change);

        // Notify the collaborator who made the change
        String ownerName = userRepository.findById(ownerId).map(User::getUsername).orElse("");
        String typeLabel = switch (change.getChangeType()) {
            case CREATE -> "新增";
            case UPDATE -> "修改";
            case DELETE -> "删除";
        };
        notificationService.create(change.getUserId(), "WORLD_CHANGE_APPROVED", worldId, "WORLD",
                ownerId, "您对世界「" + world.getName() + "」的" + typeLabel + "设定「" + change.getEntryName() + "」已被通过");

        return toVO(change);
    }

    @Override
    @Transactional
    public WorldChangeVO rejectChange(Long changeId, Long worldId, Long ownerId, String rejectReason) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "只有世界主人可以审批");
        }

        WorldChange change = changeRepository.findByIdAndWorldId(changeId, worldId)
                .orElseThrow(() -> new BusinessException(404, "修改记录不存在"));
        if (change.getStatus() != WorldChange.ChangeStatus.PENDING) {
            throw new BusinessException(400, "该修改已处理");
        }

        change.setStatus(WorldChange.ChangeStatus.REJECTED);
        change.setReviewedBy(ownerId);
        change.setRejectReason(rejectReason);
        change.setReviewedAt(java.time.LocalDateTime.now());
        changeRepository.save(change);

        String ownerName = userRepository.findById(ownerId).map(User::getUsername).orElse("");
        String reasonSuffix = (rejectReason != null && !rejectReason.isBlank())
                ? "，理由：" + rejectReason : "";
        notificationService.create(change.getUserId(), "WORLD_CHANGE_REJECTED", worldId, "WORLD",
                ownerId, "您对世界「" + world.getName() + "」的设定修改已被拒绝" + reasonSuffix);

        return toVO(change);
    }

    private WorldChangeVO toVO(WorldChange c) {
        WorldChangeVO vo = new WorldChangeVO();
        vo.setId(c.getId());
        vo.setWorldId(c.getWorldId());
        vo.setUserId(c.getUserId());
        vo.setEntryId(c.getEntryId());
        vo.setEntryName(c.getEntryName());
        vo.setEntryType(c.getEntryType());
        vo.setEntryContent(c.getEntryContent());
        vo.setChangeType(c.getChangeType().name());
        vo.setStatus(c.getStatus().name());
        vo.setRejectReason(c.getRejectReason());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setReviewedAt(c.getReviewedAt());
        return vo;
    }

    private List<WorldChangeVO> toVOList(List<WorldChange> changes) {
        if (changes.isEmpty()) return List.of();
        List<Long> userIds = changes.stream().map(WorldChange::getUserId).distinct().toList();
        Map<Long, String> nameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return changes.stream().map(c -> {
            WorldChangeVO vo = toVO(c);
            vo.setUsername(nameMap.getOrDefault(c.getUserId(), ""));
            return vo;
        }).toList();
    }
}
